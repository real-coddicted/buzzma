ALTER TABLE tickets ADD COLUMN claim_code    varchar(255);
ALTER TABLE tickets ADD COLUMN campaign_code varchar(255);

INSERT INTO ticket_categories (
    id,
    name,
    code,
    created_by,
    updated_by,
    created_at,
    updated_at,
    is_deleted
) VALUES
    ('c1c2e6a4-3b0a-4a9a-9d3a-6f5b1a9c8e21', 'Campaign', 'TICKET_CATEGORY_CAMPAIGN', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

INSERT INTO ticket_sub_categories (
    id,
    category_id,
    name,
    code,
    metadata,
    created_by,
    updated_by,
    created_at,
    updated_at,
    deleted
) VALUES
    ('d2d3f7b5-4c1b-4b0b-8e4b-7a6c2b0d9f32', 'c1c2e6a4-3b0a-4a9a-9d3a-6f5b1a9c8e21', 'General', 'TICKET_SUB_CATEGORY_GENERAL', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);