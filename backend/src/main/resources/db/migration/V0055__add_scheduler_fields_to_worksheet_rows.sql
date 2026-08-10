ALTER TABLE claim_review_worksheet_rows
    ADD COLUMN retry_count       INT                      NOT NULL DEFAULT 0,
    ADD COLUMN last_attempted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_worksheet_rows_worksheet_id
    ON claim_review_worksheet_rows (worksheet_id);

CREATE INDEX idx_worksheet_rows_processing_status
    ON claim_review_worksheet_rows (processing_status);
