-- Ticket categories (configurable from DB)
CREATE TABLE IF NOT EXISTS ticket_categories (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50)  NOT NULL,
    requires_order_id BOOLEAN    NOT NULL DEFAULT FALSE,
    requires_deal_id  BOOLEAN    NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by      UUID,
    updated_by      UUID,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ticket_categories_pkey PRIMARY KEY (id),
    CONSTRAINT ticket_categories_code_unique UNIQUE (code)
);

-- Ticket sub-categories
CREATE TABLE IF NOT EXISTS ticket_sub_categories (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    category_id UUID         NOT NULL REFERENCES ticket_categories (id),
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by  UUID,
    updated_by  UUID,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ticket_sub_categories_pkey PRIMARY KEY (id)
);

-- Seed initial categories
INSERT INTO ticket_categories (name, code, requires_order_id, requires_deal_id)
VALUES
    ('Technical Issue', 'TECH_ISSUE', FALSE, FALSE),
    ('Deal Related Issue', 'DEAL_ISSUE', TRUE, TRUE)
ON CONFLICT (code) DO NOTHING;

-- Ticket attachments
CREATE TABLE IF NOT EXISTS ticket_attachments (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    ticket_id    UUID         NOT NULL,
    uploaded_by  UUID         NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    storage_key  VARCHAR(500) NOT NULL,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by   UUID,
    updated_by   UUID,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ticket_attachments_pkey PRIMARY KEY (id)
);

-- Add new columns to tickets
ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS category_id     UUID REFERENCES ticket_categories (id),
    ADD COLUMN IF NOT EXISTS sub_category_id UUID REFERENCES ticket_sub_categories (id),
    ADD COLUMN IF NOT EXISTS raised_by       UUID,
    ADD COLUMN IF NOT EXISTS deal_id         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS assignee_id     UUID,
    ADD COLUMN IF NOT EXISTS closed_at       TIMESTAMP,
    ADD COLUMN IF NOT EXISTS title           VARCHAR(255);

-- Migrate status column from PostgreSQL ENUM type to VARCHAR so Hibernate
-- @Enumerated(STRING) can store the new Java enum names directly.
ALTER TABLE tickets ALTER COLUMN status TYPE VARCHAR(100) USING status::VARCHAR;
UPDATE tickets SET status = 'TICKET_STATUS_OPEN'   WHERE status = 'Open';
UPDATE tickets SET status = 'TICKET_STATUS_CLOSED'  WHERE status = 'Resolved';
UPDATE tickets SET status = 'TICKET_STATUS_CLOSED'  WHERE status = 'Rejected';
DROP TYPE IF EXISTS ticket_status;

-- Align ticket_comments to new entity (add author_id, content; keep existing rows)
ALTER TABLE ticket_comments
    ADD COLUMN IF NOT EXISTS author_id UUID,
    ADD COLUMN IF NOT EXISTS content   VARCHAR(2000);

-- Back-fill author_id and content from legacy columns where present
UPDATE ticket_comments SET author_id = user_id  WHERE author_id IS NULL AND user_id IS NOT NULL;
UPDATE ticket_comments SET content   = message  WHERE content   IS NULL AND message  IS NOT NULL;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_ticket_categories_is_active ON ticket_categories (is_active) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_ticket_sub_categories_category_id ON ticket_sub_categories (category_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tickets_raised_by ON tickets (raised_by) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tickets_assignee_id ON tickets (assignee_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_ticket_attachments_ticket_id ON ticket_attachments (ticket_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_ticket_comments_ticket_id ON ticket_comments (ticket_id) WHERE is_deleted = FALSE;
