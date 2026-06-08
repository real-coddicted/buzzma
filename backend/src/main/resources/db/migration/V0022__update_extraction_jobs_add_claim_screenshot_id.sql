DROP INDEX IF EXISTS idx_extraction_jobs_submitted_by;

ALTER TABLE extraction_jobs DROP COLUMN submitted_by;
ALTER TABLE extraction_jobs DROP COLUMN storage_key;
ALTER TABLE extraction_jobs DROP COLUMN original_filename;
ALTER TABLE extraction_jobs DROP COLUMN content_type;
ALTER TABLE extraction_jobs DROP COLUMN result;
ALTER TABLE extraction_jobs DROP COLUMN validation_errors;

ALTER TABLE extraction_jobs
    ADD COLUMN claim_screenshot_id UUID NOT NULL REFERENCES claim_screenshots(id);

CREATE INDEX idx_extraction_jobs_claim_screenshot_id ON extraction_jobs(claim_screenshot_id);
