-- ============================================================
-- Config Service — core schema (PostgreSQL)
-- ============================================================
-- Applies to every environment. No seed data here.
--
-- Design rationale: docs/architecture/config-service-design.md
-- ============================================================

-- ---------- Enums ----------

CREATE TYPE value_type_enum AS ENUM ('boolean', 'string', 'number', 'json');

-- Only 'static' evaluation is supported now. Adding targeting/rollout later
-- is an additive ALTER TYPE — do not build rule evaluation until that is
-- explicitly picked up (see design doc §9).
CREATE TYPE evaluation_type_enum AS ENUM ('static');

CREATE TYPE entry_status_enum AS ENUM ('active', 'deprecated', 'deleted');

-- ---------- Global version sequence ----------
-- Shared across every row and write. Gives a strict global ordering so the
-- SDK's delta-poll query (WHERE version > :since_version) is a cheap indexed
-- range scan rather than a timestamp comparison.

CREATE SEQUENCE config_version_seq;

-- ---------- Main table ----------

CREATE TABLE config_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Namespace represents a logical grouping (team or service). May map 1:1
    -- to a microservice today, but not guaranteed — hence "namespace" not
    -- "service_name" (see design doc §3).
    namespace       VARCHAR(100) NOT NULL,

    -- Misconfiguration safety net: each deployment of the Config API knows
    -- which environment it serves and asserts this column on startup. It is
    -- NOT the prod/non-prod isolation boundary — a separate DB instance is
    -- (see design doc §4).
    environment     VARCHAR(50)  NOT NULL,

    key             VARCHAR(200) NOT NULL,

    value_type      value_type_enum NOT NULL,

    -- Stores boolean/string/number/json uniformly. The CHECK constraint below
    -- enforces that the stored value actually matches its declared type.
    value           JSONB NOT NULL,

    -- Extension point for future targeting/rollout rules. Currently only
    -- 'static' is allowed. Do not populate `rules` until the rule engine is
    -- designed (see design doc §9).
    evaluation_type evaluation_type_enum NOT NULL DEFAULT 'static',
    rules           JSONB,

    -- Soft delete: history stays intact; a deleted-then-recreated key does
    -- not lose its audit trail.
    status          entry_status_enum NOT NULL DEFAULT 'active',

    description     TEXT,
    owner           VARCHAR(100),

    version         BIGINT NOT NULL DEFAULT nextval('config_version_seq'),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Must be set from the authenticated caller's identity by the API service,
    -- never trusted from the request body. Flyway-authored rows use 'flyway'.
    updated_by      VARCHAR(100) NOT NULL,

    CONSTRAINT uq_config_entry UNIQUE (namespace, environment, key),

    CONSTRAINT chk_value_type_match CHECK (
        (value_type = 'boolean' AND jsonb_typeof(value) = 'boolean') OR
        (value_type = 'string'  AND jsonb_typeof(value) = 'string')  OR
        (value_type = 'number'  AND jsonb_typeof(value) = 'number')  OR
        (value_type = 'json'    AND jsonb_typeof(value) IN ('object', 'array'))
    )
);

-- Fast path for SDK bulk startup fetch: all active configs for a namespace+env
CREATE INDEX idx_config_lookup
    ON config_entries (namespace, environment)
    WHERE status = 'active';

-- Fast path for SDK delta poll: changes since version N for a namespace+env
CREATE INDEX idx_config_delta_poll
    ON config_entries (namespace, environment, version);

-- ---------- History table (append-only audit trail) ----------
-- Kept separate from config_entries so the main table stays small and its
-- indexes stay fast. History can grow indefinitely.

CREATE TABLE config_entries_history (
    history_id      BIGSERIAL PRIMARY KEY,

    entry_id        UUID NOT NULL,
    namespace       VARCHAR(100) NOT NULL,
    environment     VARCHAR(50)  NOT NULL,
    key             VARCHAR(200) NOT NULL,

    old_value       JSONB,
    new_value       JSONB,
    old_status      entry_status_enum,
    new_status      entry_status_enum,

    version         BIGINT NOT NULL,
    changed_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    changed_by      VARCHAR(100) NOT NULL,
    change_reason   TEXT
);

CREATE INDEX idx_history_entry
    ON config_entries_history (entry_id, changed_at DESC);

-- ---------- Trigger: auto-version + auto-audit ----------
-- Version bumping and history writes happen in the DB, not application code,
-- so every change is captured consistently regardless of write path (API,
-- migration, console session).

CREATE OR REPLACE FUNCTION fn_config_entries_audit() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO config_entries_history
            (entry_id, namespace, environment, key,
             old_value, new_value, old_status, new_status,
             version, changed_by, change_reason)
        VALUES
            (NEW.id, NEW.namespace, NEW.environment, NEW.key,
             NULL, NEW.value, NULL, NEW.status,
             NEW.version, NEW.updated_by, 'created');

    ELSIF TG_OP = 'UPDATE' THEN
        NEW.version    := nextval('config_version_seq');
        NEW.updated_at := now();

        INSERT INTO config_entries_history
            (entry_id, namespace, environment, key,
             old_value, new_value, old_status, new_status,
             version, changed_by, change_reason)
        VALUES
            (NEW.id, NEW.namespace, NEW.environment, NEW.key,
             OLD.value, NEW.value, OLD.status, NEW.status,
             NEW.version, NEW.updated_by, 'updated');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_config_entries_audit
    BEFORE INSERT OR UPDATE ON config_entries
    FOR EACH ROW EXECUTE FUNCTION fn_config_entries_audit();
