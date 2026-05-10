-- ============================================================
-- Baseline schema derived from JPA entity definitions.
--
-- NOTE: SecurityQuestion entity has @Table(name = "security_answers")
-- which conflicts with SecurityAnswer. Using "security_questions" here.
--
-- NOTE: UserCredential entity has @Table(name = "users") which conflicts
-- with BuzzmaUser. Using "user_credentials" here.
-- ============================================================

-- ============================================================
-- IDENTITY MODULE
-- ============================================================

CREATE TABLE users (
    id          uuid         NOT NULL,
    name        varchar(120) NOT NULL,
    username    varchar(64)  UNIQUE,
    mobile      varchar(10)  NOT NULL,
    email       varchar(320),
    role        varchar(50),
    status      varchar(50),
    created_by  uuid,
    updated_by  uuid,
    created_at  timestamp with time zone  NOT NULL,
    updated_at  timestamp with time zone  NOT NULL,
    is_deleted  boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE user_credentials (
    id             uuid    NOT NULL,
    user_id        uuid    NOT NULL,
    password_hash  text    NOT NULL,
    created_by     uuid,
    updated_by     uuid,
    created_at     timestamp with time zone  NOT NULL,
    updated_at     timestamp with time zone  NOT NULL,
    is_deleted     boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_user_credentials PRIMARY KEY (id),
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE user_banking_details (
    id                   uuid NOT NULL,
    account_number       varchar(255),
    ifsc_code            varchar(255),
    bank_name            varchar(255),
    account_holder_name  varchar(255),
    created_by           uuid,
    updated_by           uuid,
    created_at           timestamp with time zone  NOT NULL,
    updated_at           timestamp with time zone  NOT NULL,
    is_deleted           boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_user_banking_details PRIMARY KEY (id)
);

CREATE TABLE security_questions (
    id          uuid         NOT NULL,
    question    varchar(255) NOT NULL,
    created_by  uuid         NOT NULL,
    updated_by  uuid,
    created_at  timestamp with time zone  NOT NULL,
    updated_at  timestamp with time zone  NOT NULL,
    is_deleted  boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_security_questions PRIMARY KEY (id)
);

CREATE TABLE security_answers (
    id           uuid NOT NULL,
    user_id      uuid NOT NULL,
    question_id  uuid NOT NULL,
    answer_hash  text NOT NULL,
    created_by   uuid,
    updated_by   uuid,
    created_at   timestamp with time zone  NOT NULL,
    updated_at   timestamp with time zone  NOT NULL,
    is_deleted   boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_security_answers PRIMARY KEY (id),
    CONSTRAINT fk_security_answers_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_security_answers_question FOREIGN KEY (question_id) REFERENCES security_questions (id)
);

CREATE TABLE invites (
    id            uuid         NOT NULL,
    code          varchar(255) NOT NULL UNIQUE,
    invitee_role  varchar(50)  NOT NULL,
    owner_id      uuid,
    status        varchar(50),
    valid_to      integer,
    created_by    uuid,
    updated_by    uuid,
    created_at    timestamp with time zone  NOT NULL,
    updated_at    timestamp with time zone  NOT NULL,
    is_deleted    boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_invites PRIMARY KEY (id)
);

-- ============================================================
-- SETTINGS MODULE
-- ============================================================

CREATE TABLE user_settings (
    id          uuid NOT NULL,
    user_id     uuid NOT NULL,
    settings    jsonb,
    created_by  uuid,
    updated_by  uuid,
    created_at  timestamp with time zone  NOT NULL,
    updated_at  timestamp with time zone  NOT NULL,
    is_deleted  boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_user_settings PRIMARY KEY (id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ============================================================
-- CAMPAIGN MODULE
-- ============================================================

CREATE TABLE products (
    id            uuid         NOT NULL,
    name          varchar(255) NOT NULL,
    image_url     text         NOT NULL,
    product_link  text         NOT NULL,
    price_paise   numeric      NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE campaigns (
    id           uuid         NOT NULL,
    title        varchar(200) NOT NULL,
    owner_id     uuid         NOT NULL,
    total_slots  integer      NOT NULL,
    product_id   uuid         NOT NULL,
    platform     varchar(50)  NOT NULL,
    type         varchar(50),
    status       varchar(50),
    end_date     integer,
    open_to_all  boolean      NOT NULL DEFAULT false,
    created_by   uuid,
    updated_by   uuid,
    created_at   timestamp with time zone  NOT NULL,
    updated_at   timestamp with time zone  NOT NULL,
    is_deleted   boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_campaigns         PRIMARY KEY (id),
    CONSTRAINT fk_campaigns_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE campaign_slots (
    id               uuid    NOT NULL,
    campaign_id      uuid    NOT NULL,
    total_slots      integer NOT NULL,
    slots_available  integer NOT NULL,
    created_at       timestamp with time zone  NOT NULL,
    updated_at       timestamp with time zone  NOT NULL,
    created_by       uuid                      NOT NULL,
    updated_by       uuid,
    is_deleted       boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_campaign_slots          PRIMARY KEY (id),
    CONSTRAINT fk_campaign_slots_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
);

CREATE TABLE campaign_assignments (
    id                        uuid    NOT NULL,
    campaign_id               uuid    NOT NULL,
    assignor_id               uuid    NOT NULL,
    assignee_id               uuid    NOT NULL,
    slot_limit                integer NOT NULL,
    campaign_price_paise      numeric NOT NULL,
    commission_offered_paise  numeric,
    status                    varchar(50),
    slot_id                   uuid    NOT NULL,
    created_at                timestamp with time zone  NOT NULL,
    updated_at                timestamp with time zone  NOT NULL,
    created_by                uuid,
    updated_by                uuid,
    is_deleted                boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_campaign_assignments      PRIMARY KEY (id),
    CONSTRAINT fk_campaign_assignments_slot FOREIGN KEY (slot_id) REFERENCES campaign_slots (id)
);

CREATE TABLE commissions (
    id             uuid    NOT NULL,
    campaign_id    uuid    NOT NULL,
    charged_by_id  uuid,
    commission     numeric,
    CONSTRAINT pk_commissions          PRIMARY KEY (id),
    CONSTRAINT fk_commissions_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
);

-- ============================================================
-- SUPPORT MODULE
-- ============================================================

CREATE TABLE ticket_categories (
    id          uuid         NOT NULL,
    name        varchar(100) NOT NULL,
    code        varchar(50)  NOT NULL UNIQUE,
    created_by  uuid,
    updated_by  uuid,
    created_at  timestamp with time zone  NOT NULL,
    updated_at  timestamp with time zone  NOT NULL,
    is_deleted  boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_ticket_categories PRIMARY KEY (id)
);

-- column name is "deleted" as defined in entity (not "is_deleted")
CREATE TABLE ticket_sub_categories (
    id           uuid         NOT NULL,
    category_id  uuid         NOT NULL,
    name         varchar(100) NOT NULL,
    code         varchar(50)  NOT NULL,
    metadata     jsonb,
    created_by   uuid,
    updated_by   uuid,
    created_at   timestamp with time zone  NOT NULL,
    updated_at   timestamp with time zone  NOT NULL,
    deleted      boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_ticket_sub_categories          PRIMARY KEY (id),
    CONSTRAINT fk_ticket_sub_categories_category FOREIGN KEY (category_id) REFERENCES ticket_categories (id)
);

-- column name is "deleted" as defined in entity (not "is_deleted")
CREATE TABLE tickets (
    id               uuid         NOT NULL,
    category_id      uuid         NOT NULL,
    sub_category_id  uuid         NOT NULL,
    raised_by        uuid         NOT NULL,
    description      text         NOT NULL,
    order_id         varchar(255),
    status           varchar(100) NOT NULL,
    assignee_id      uuid,
    created_by       uuid,
    updated_by       uuid,
    created_at       timestamp with time zone  NOT NULL,
    updated_at       timestamp with time zone  NOT NULL,
    deleted          boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_tickets              PRIMARY KEY (id),
    CONSTRAINT fk_tickets_category     FOREIGN KEY (category_id)     REFERENCES ticket_categories (id),
    CONSTRAINT fk_tickets_sub_category FOREIGN KEY (sub_category_id) REFERENCES ticket_sub_categories (id)
);

CREATE TABLE ticket_attachments (
    id            uuid         NOT NULL,
    ticket_id     uuid         NOT NULL,
    uploaded_by   uuid         NOT NULL,
    file_name     varchar(255) NOT NULL,
    content_type  varchar(100) NOT NULL,
    size_bytes    bigint       NOT NULL,
    storage_key   varchar(500) NOT NULL,
    is_deleted    boolean      NOT NULL DEFAULT false,
    created_by    uuid,
    updated_by    uuid,
    created_at    timestamp with time zone  NOT NULL,
    updated_at    timestamp with time zone  NOT NULL,
    CONSTRAINT pk_ticket_attachments        PRIMARY KEY (id),
    CONSTRAINT fk_ticket_attachments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);

CREATE TABLE ticket_comments (
    id          uuid          NOT NULL,
    ticket_id   uuid          NOT NULL,
    author_id   uuid          NOT NULL,
    content     varchar(2000) NOT NULL,
    created_by  uuid,
    updated_by  uuid,
    created_at  timestamp with time zone  NOT NULL,
    updated_at  timestamp with time zone  NOT NULL,
    is_deleted  boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_ticket_comments        PRIMARY KEY (id),
    CONSTRAINT fk_ticket_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);

-- ============================================================
-- FEEDBACK MODULE
-- ============================================================

CREATE TABLE feedback (
    id          uuid    NOT NULL,
    user_id     uuid    NOT NULL,
    rating      integer NOT NULL,
    category    varchar(50)  NOT NULL,
    feedback    text         NOT NULL,
    created_by  uuid,
    updated_by  uuid,
    created_at  timestamp with time zone  NOT NULL,
    updated_at  timestamp with time zone  NOT NULL,
    is_deleted  boolean                   NOT NULL DEFAULT false,
    CONSTRAINT pk_feedback      PRIMARY KEY (id),
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_users_mobile     ON users (mobile);
CREATE INDEX idx_users_is_deleted ON users (is_deleted);

CREATE INDEX idx_user_credentials_user_id ON user_credentials (user_id);

CREATE INDEX idx_security_answers_user_id     ON security_answers (user_id);
CREATE INDEX idx_security_answers_question_id ON security_answers (question_id);

CREATE INDEX idx_invites_owner_id ON invites (owner_id);
CREATE INDEX idx_invites_status   ON invites (status);

CREATE INDEX idx_user_settings_user_id ON user_settings (user_id);

CREATE INDEX idx_campaigns_owner_id   ON campaigns (owner_id);
CREATE INDEX idx_campaigns_status     ON campaigns (status);
CREATE INDEX idx_campaigns_platform   ON campaigns (platform);
CREATE INDEX idx_campaigns_is_deleted ON campaigns (is_deleted);

CREATE INDEX idx_campaign_slots_campaign_id ON campaign_slots (campaign_id);

CREATE INDEX idx_campaign_assignments_campaign_id ON campaign_assignments (campaign_id);
CREATE INDEX idx_campaign_assignments_assignee_id ON campaign_assignments (assignee_id);
CREATE INDEX idx_campaign_assignments_assignor_id ON campaign_assignments (assignor_id);

CREATE INDEX idx_tickets_raised_by   ON tickets (raised_by);
CREATE INDEX idx_tickets_status      ON tickets (status);
CREATE INDEX idx_tickets_category_id ON tickets (category_id);
CREATE INDEX idx_tickets_assignee_id ON tickets (assignee_id);

CREATE INDEX idx_ticket_attachments_ticket_id ON ticket_attachments (ticket_id);

CREATE INDEX idx_ticket_comments_ticket_id ON ticket_comments (ticket_id);
