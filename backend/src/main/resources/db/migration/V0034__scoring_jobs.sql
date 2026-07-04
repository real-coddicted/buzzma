-- ============================================================
-- SCORING MODULE
-- ============================================================

CREATE TABLE scoring_jobs (
    id                  uuid                     NOT NULL,
    claim_screenshot_id uuid                     NOT NULL REFERENCES claim_screenshots(id),
    status              varchar(50)              NOT NULL,
    attempt_count       integer                  NOT NULL DEFAULT 0,
    error_message       text,
    created_by          uuid,
    updated_by          uuid,
    created_at          timestamp with time zone NOT NULL,
    updated_at          timestamp with time zone NOT NULL,
    is_deleted          boolean                  NOT NULL DEFAULT false,
    CONSTRAINT pk_scoring_jobs PRIMARY KEY (id)
);

CREATE INDEX idx_scoring_jobs_claim_screenshot_id ON scoring_jobs(claim_screenshot_id);
CREATE INDEX idx_scoring_jobs_status ON scoring_jobs(status);
CREATE INDEX idx_scoring_jobs_is_deleted ON scoring_jobs(is_deleted);
