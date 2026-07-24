-- ============================================================
-- Config Service — core schema (PostgreSQL)
-- ============================================================
-- Applies to every environment. No seed data here.
--
-- Design rationale: docs/architecture/config-service-design.md
-- ============================================================

-- ---------- Enums ----------

-- value_type is stable by design (maps 1:1 to JSON primitive types) so a
-- native PG enum is appropriate here.
DO $$ BEGIN
    CREATE TYPE value_type_enum AS ENUM ('boolean', 'string', 'number', 'json');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- evaluation_type and entry_status are expected to grow over time; they use
-- VARCHAR + CHECK constraints so adding or removing values is a simple ALTER
-- TABLE rather than a full type recreation.

-- ---------- Global change sequence ----------
-- Shared across every row and write. Gives a strict global ordering so the
-- SDK's delta-poll query (WHERE change_seq > :since_change_seq) is a cheap
-- indexed range scan rather than a timestamp comparison.

CREATE SEQUENCE IF NOT EXISTS config_change_seq;

-- ---------- Main table ----------

CREATE TABLE IF NOT EXISTS config_entries (
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
    evaluation_type VARCHAR(50) NOT NULL DEFAULT 'STATIC',
    rules           JSONB,

    -- Soft delete: history stays intact; a deleted-then-recreated key does
    -- not lose its audit trail.
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    description     TEXT,
    owner           VARCHAR(100),

    change_seq      BIGINT NOT NULL DEFAULT nextval('config_change_seq'),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Must be set from the authenticated caller's identity by the API service,
    -- never trusted from the request body. Flyway-authored rows use 'flyway'.
    updated_by      VARCHAR(100) NOT NULL,

    CONSTRAINT uq_config_entry UNIQUE (namespace, environment, key),

    CONSTRAINT chk_evaluation_type CHECK (evaluation_type IN ('STATIC')),

    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'DELETED')),

    CONSTRAINT chk_value_type_match CHECK (
        (value_type = 'boolean' AND jsonb_typeof(value) = 'boolean') OR
        (value_type = 'string'  AND jsonb_typeof(value) = 'string')  OR
        (value_type = 'number'  AND jsonb_typeof(value) = 'number')  OR
        (value_type = 'json'    AND jsonb_typeof(value) IN ('object', 'array'))
    )
);

-- Fast path for SDK bulk startup fetch: all active configs for a namespace+env
CREATE INDEX IF NOT EXISTS idx_config_lookup
    ON config_entries (namespace, environment)
    WHERE status = 'ACTIVE';

-- Fast path for SDK delta poll: changes since change_seq N for a namespace+env
CREATE INDEX IF NOT EXISTS idx_config_delta_poll
    ON config_entries (namespace, environment, change_seq);

-- ---------- History table (append-only audit trail) ----------
-- Kept separate from config_entries so the main table stays small and its
-- indexes stay fast. History can grow indefinitely.

CREATE TABLE IF NOT EXISTS config_entries_history (
    history_id      BIGSERIAL PRIMARY KEY,

    entry_id        UUID NOT NULL,
    namespace       VARCHAR(100) NOT NULL,
    environment     VARCHAR(50)  NOT NULL,
    key             VARCHAR(200) NOT NULL,

    old_value       JSONB,
    new_value       JSONB,
    old_status      VARCHAR(50),
    new_status      VARCHAR(50),

    change_seq      BIGINT NOT NULL,
    changed_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    changed_by      VARCHAR(100) NOT NULL,
    change_reason   TEXT
);

CREATE INDEX IF NOT EXISTS idx_history_entry
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
             change_seq, changed_by, change_reason)
        VALUES
            (NEW.id, NEW.namespace, NEW.environment, NEW.key,
             NULL, NEW.value, NULL, NEW.status,
             NEW.change_seq, NEW.updated_by, 'created');

    ELSIF TG_OP = 'UPDATE' THEN
        NEW.change_seq  := nextval('config_change_seq');
        NEW.updated_at  := now();

        INSERT INTO config_entries_history
            (entry_id, namespace, environment, key,
             old_value, new_value, old_status, new_status,
             change_seq, changed_by, change_reason)
        VALUES
            (NEW.id, NEW.namespace, NEW.environment, NEW.key,
             OLD.value, NEW.value, OLD.status, NEW.status,
             NEW.change_seq, NEW.updated_by, 'updated');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_config_entries_audit
    BEFORE INSERT OR UPDATE ON config_entries
    FOR EACH ROW EXECUTE FUNCTION fn_config_entries_audit();
