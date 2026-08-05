ALTER TABLE claims
    ADD COLUMN accounting_status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN accounting_retry_count       INT          NOT NULL DEFAULT 0,
    ADD COLUMN accounting_last_attempted_at TIMESTAMPTZ;

CREATE INDEX idx_claims_accounting_pending
    ON claims (review_status, updated_at)
    WHERE accounting_status IN ('PENDING', 'FAILED') AND is_deleted = false;
