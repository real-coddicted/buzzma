-- ============================================================
-- CAMPAIGN DEAL FIELDS
-- ============================================================

ALTER TABLE campaigns ADD COLUMN campaign_price_paise NUMERIC;
ALTER TABLE campaigns ADD COLUMN return_window_days   INTEGER;
ALTER TABLE campaigns ADD COLUMN terms_and_conditions TEXT;
ALTER TABLE campaigns ADD COLUMN seller_name          VARCHAR(255);

-- ============================================================
-- ORDER MODULE
-- ============================================================

CREATE TABLE orders (
    id                    uuid                     NOT NULL,
    campaign_id           uuid                     NOT NULL,
    buyer_id              uuid                     NOT NULL,
    status                varchar(50)              NOT NULL,
    ecommerce_order_id    varchar(100),
    amount_paise          NUMERIC,
    product_name          varchar(255),
    seller_name           varchar(255),
    order_date            varchar(10),
    account_name          varchar(255),
    review_url            varchar(500),
    order_screenshot_key  varchar(500),
    review_screenshot_key varchar(500),
    return_screenshot_key varchar(500),
    rejection_note        TEXT,
    created_by            uuid,
    updated_by            uuid,
    created_at            timestamp with time zone NOT NULL,
    updated_at            timestamp with time zone NOT NULL,
    is_deleted            boolean                  NOT NULL DEFAULT false,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users (id)
);

CREATE INDEX idx_orders_buyer_id    ON orders (buyer_id);
CREATE INDEX idx_orders_campaign_id ON orders (campaign_id);
CREATE INDEX idx_orders_status      ON orders (status);
CREATE INDEX idx_orders_is_deleted  ON orders (is_deleted);

CREATE UNIQUE INDEX uq_orders_buyer_campaign ON orders (buyer_id, campaign_id);
