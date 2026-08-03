CREATE TABLE campaign_brand_shares (
    id                  uuid    NOT NULL,
    campaign_id         uuid    NOT NULL,
    brand_user_id       uuid    NOT NULL,
    shared_by_user_id   uuid    NOT NULL,
    created_at          timestamp with time zone  NOT NULL,
    updated_at          timestamp with time zone  NOT NULL,
    created_by          uuid,
    updated_by          uuid,
    CONSTRAINT pk_campaign_brand_shares            PRIMARY KEY (id),
    CONSTRAINT fk_campaign_brand_shares_campaign    FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
    CONSTRAINT uq_campaign_brand_shares_campaign_id UNIQUE (campaign_id)
);

ALTER TABLE claims ADD COLUMN brand_reviewer_id UUID;
ALTER TABLE claims ADD COLUMN brand_review_status VARCHAR(50);
ALTER TABLE claims ADD COLUMN brand_approved_amount_paise NUMERIC;