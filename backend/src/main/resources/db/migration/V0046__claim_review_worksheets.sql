CREATE TABLE claim_review_worksheets (
    id            UUID          NOT NULL DEFAULT gen_random_uuid(),
    uploaded_by   UUID          NOT NULL,
    original_filename VARCHAR(500),
    storage_key   VARCHAR(500)  NOT NULL,
    row_count     INT           NOT NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    created_by    UUID,
    updated_by    UUID,
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL,
    PRIMARY KEY (id)
);
