CREATE TABLE campaign_shares (
    id                  uuid    NOT NULL,
    campaign_id         uuid    NOT NULL,
    from_user_id        uuid    NOT NULL,
    to_user_id          uuid    NOT NULL,
    created_at          timestamp with time zone  NOT NULL,
    updated_at          timestamp with time zone  NOT NULL,
    created_by          uuid,
    updated_by          uuid,
    CONSTRAINT pk_campaign_shares            PRIMARY KEY (id),
    CONSTRAINT fk_campaign_shares_campaign    FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
    CONSTRAINT uq_campaign_shares_campaign_id UNIQUE (campaign_id)
);