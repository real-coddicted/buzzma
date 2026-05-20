-- ============================================================
-- DEAL MODULE
-- ============================================================

CREATE TABLE deals (
    id                uuid                     NOT NULL,
    owner_id          uuid                     NOT NULL,
    campaign_id       uuid                     NOT NULL,
    slot_id           uuid                     NOT NULL,
    deal_price_paise  NUMERIC                  NOT NULL,
    created_by        uuid,
    updated_by        uuid,
    created_at        timestamp with time zone NOT NULL,
    updated_at        timestamp with time zone NOT NULL,
    is_deleted        boolean                  NOT NULL DEFAULT false,
    CONSTRAINT pk_deals          PRIMARY KEY (id),
    CONSTRAINT fk_deals_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
    CONSTRAINT fk_deals_slot     FOREIGN KEY (slot_id)     REFERENCES campaign_slots (id)
);

CREATE INDEX idx_deals_campaign_id ON deals (campaign_id);
CREATE INDEX idx_deals_owner_id    ON deals (owner_id);
CREATE INDEX idx_deals_is_deleted  ON deals (is_deleted);

-- ============================================================
-- CLAIM MODULE
-- ============================================================

CREATE TABLE claims (
    id                   uuid                     NOT NULL,
    campaign_id          uuid                     NOT NULL,
    deal_id              uuid                     NOT NULL,
    owner_id             uuid                     NOT NULL,
    status               varchar(50)              NOT NULL,
    ecommerce_order_id   varchar(100),
    amount_paise         NUMERIC,
    product_name         varchar(255),
    seller_name          varchar(255),
    order_date           varchar(10),
    account_name         varchar(255),
    review_url           varchar(500),
    overall_verified     boolean,
    overall_score        double precision,
    rejection_note       TEXT,
    comments             TEXT,
    created_by           uuid,
    updated_by           uuid,
    created_at           timestamp with time zone NOT NULL,
    updated_at           timestamp with time zone NOT NULL,
    is_deleted           boolean                  NOT NULL DEFAULT false,
    CONSTRAINT pk_claims         PRIMARY KEY (id),
    CONSTRAINT fk_claims_deal    FOREIGN KEY (deal_id)    REFERENCES deals (id),
    CONSTRAINT fk_claims_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
);

CREATE INDEX idx_claims_campaign_id ON claims (campaign_id);
CREATE INDEX idx_claims_deal_id     ON claims (deal_id);
CREATE INDEX idx_claims_owner_id    ON claims (owner_id);
CREATE INDEX idx_claims_status      ON claims (is_deleted);

-- ============================================================
-- CLAIM SCREENSHOTS
-- ============================================================

CREATE TABLE claim_screenshots (
    id                  uuid                     NOT NULL,
    claim_id            uuid                     NOT NULL,
    "key"               varchar(500)             NOT NULL,
    type                varchar(50)              NOT NULL,
    verification_status varchar(50)              NOT NULL,
    score               double precision,
    extracted_details   jsonb,
    created_by          uuid,
    updated_by          uuid,
    created_at          timestamp with time zone NOT NULL,
    updated_at          timestamp with time zone NOT NULL,
    is_deleted          boolean                  NOT NULL DEFAULT false,
    CONSTRAINT pk_claim_screenshots       PRIMARY KEY (id),
    CONSTRAINT fk_claim_screenshots_claim FOREIGN KEY (claim_id) REFERENCES claims (id)
);

CREATE INDEX idx_claim_screenshots_claim_id ON claim_screenshots (claim_id);
