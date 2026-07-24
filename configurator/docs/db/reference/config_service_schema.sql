-- ============================================================
-- Config Service — Database Schema (PostgreSQL)
-- ============================================================
--
-- Environment isolation:
-- Prod runs on its own DB instance, its own credentials, and its own
-- deployment of the Config API service — never reachable from non-prod.
-- Lower environments (dev, staging, qa) may share a single DB instance,
-- using the `environment` column below to separate their data.
--
-- Because of this, `environment` is NOT the isolation mechanism between
-- prod and non-prod — the separate DB instance is. Within any single DB
-- instance, every row already belongs to that instance's environment by
-- definition, so the column is redundant for filtering purposes there.
--
-- It's kept anyway as a misconfiguration safety net: each deployment of
-- the Config API service knows which environment it's supposed to be
-- serving, and should assert that on startup against the rows it reads,
-- e.g. refuse to start if it expects `staging` but the DB contains
-- `prod` rows. This turns a silent wrong-DB connection mistake into a
-- loud startup failure instead of quietly serving the wrong config.
-- It also preserves provenance if data is ever copied out of its
-- original DB (backup restores, debugging snapshots, audit exports).
-- ============================================================

-- ---------- Enums ----------

CREATE TYPE value_type_enum AS ENUM ('boolean', 'string', 'number', 'json');

-- Reserved for future targeting/rollout support.
-- Add 'rule_based' later via: ALTER TYPE evaluation_type_enum ADD VALUE 'rule_based';
CREATE TYPE evaluation_type_enum AS ENUM ('static');

CREATE TYPE entry_status_enum AS ENUM ('active', 'deleted');

-- ---------- Global change sequence ----------
-- One sequence shared by every row/write. This gives us a single
-- monotonically increasing counter across the whole table, which is
-- what makes "give me everything changed since change_seq N" (the SDK's
-- delta-poll query) cheap and correct. It also doubles as an
-- optimistic-concurrency token for the admin UI.

CREATE SEQUENCE config_change_seq;

-- ---------- Main table (current state) ----------

CREATE TABLE config_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    namespace       VARCHAR(100) NOT NULL,
    environment     VARCHAR(50)  NOT NULL,  -- misconfiguration safety net, not the isolation
                                             -- boundary — see header note. Prod isolation comes
                                             -- from running on a separate DB instance entirely.
    key             VARCHAR(200) NOT NULL,

    value_type      value_type_enum NOT NULL,
    value           JSONB NOT NULL,          -- holds bool/string/number/json uniformly

    evaluation_type evaluation_type_enum NOT NULL DEFAULT 'static',
    rules           JSONB,                   -- reserved, NULL until targeting is built

    status          entry_status_enum NOT NULL DEFAULT 'active',

    description     TEXT,
    owner           VARCHAR(100),

    change_seq      BIGINT NOT NULL DEFAULT nextval('config_change_seq'),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by      VARCHAR(100) NOT NULL,

    CONSTRAINT uq_config_entry UNIQUE (namespace, environment, key),

    -- Defense in depth: DB refuses to store a value that doesn't match
    -- its declared type, even if the app layer has a bug.
    CONSTRAINT chk_value_type_match CHECK (
        (value_type = 'boolean' AND jsonb_typeof(value) = 'boolean') OR
        (value_type = 'string'  AND jsonb_typeof(value) = 'string')  OR
        (value_type = 'number'  AND jsonb_typeof(value) = 'number')  OR
        (value_type = 'json'    AND jsonb_typeof(value) IN ('object', 'array'))
    )
);

-- Fast path for SDK's bulk startup fetch: "all active configs for service+env"
CREATE INDEX idx_config_lookup
    ON config_entries (namespace, environment)
    WHERE status = 'active';

-- Fast path for SDK's delta poll: "changes since change_seq N for service+env"
CREATE INDEX idx_config_delta_poll
    ON config_entries (namespace, environment, change_seq);

-- ---------- History table (append-only audit trail) ----------
-- Kept separate from the main table so config_entries stays small and
-- fast to read/index, while history can grow indefinitely and be
-- queried independently for compliance/rollback purposes.

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

    change_seq      BIGINT NOT NULL,
    changed_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    changed_by      VARCHAR(100) NOT NULL,
    change_reason   TEXT
);

CREATE INDEX idx_history_entry
    ON config_entries_history (entry_id, changed_at DESC);

-- ---------- Trigger: auto-version + auto-audit ----------
-- Bumping the version and writing the history row happens in the DB
-- trigger, not application code. This guarantees every change is
-- captured and versioned consistently, even if someone edits the row
-- through a different code path (a script, a migration, a console).

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

CREATE TRIGGER trg_config_entries_audit
    BEFORE INSERT OR UPDATE ON config_entries
    FOR EACH ROW EXECUTE FUNCTION fn_config_entries_audit();

-- ============================================================
-- Reference queries the app/SDK will actually run
-- ============================================================

-- SDK bulk fetch on startup:
-- SELECT * FROM config_entries
-- WHERE namespace = :service AND environment = :env AND status = 'active';

-- SDK delta poll (every 30-60s):
-- SELECT * FROM config_entries
-- WHERE namespace = :service AND environment = :env
--   AND change_seq > :since_change_seq AND status != 'deleted'
-- ORDER BY change_seq;

-- Admin UI optimistic-concurrency update (prevents two admins clobbering
-- each other's edits — the trigger still bumps change_seq automatically):
-- UPDATE config_entries
-- SET value = :new_value, updated_by = :admin_user
-- WHERE id = :entry_id AND change_seq = :expected_change_seq;
-- (zero rows affected = someone else changed it since you loaded it — reload and retry)
