-- ============================================================
-- EXTRACTION MODULE
-- ============================================================

CREATE TABLE extraction_jobs (
    id                uuid                     NOT NULL,
    submitted_by      uuid                     NOT NULL,
    status            varchar(50)              NOT NULL,
    storage_key       varchar(500),
    original_filename varchar(255),
    content_type      varchar(100),
    attempt_count     integer                  NOT NULL DEFAULT 0,
    error_message     text,
    result            jsonb,
    validation_errors jsonb,
    created_by        uuid,
    updated_by        uuid,
    created_at        timestamp with time zone NOT NULL,
    updated_at        timestamp with time zone NOT NULL,
    is_deleted        boolean                  NOT NULL DEFAULT false,
    CONSTRAINT pk_extraction_jobs PRIMARY KEY (id),
    CONSTRAINT fk_extraction_jobs_user FOREIGN KEY (submitted_by) REFERENCES users (id)
);

CREATE INDEX idx_extraction_jobs_submitted_by ON extraction_jobs (submitted_by);
CREATE INDEX idx_extraction_jobs_status ON extraction_jobs (status);
CREATE INDEX idx_extraction_jobs_is_deleted ON extraction_jobs (is_deleted);
