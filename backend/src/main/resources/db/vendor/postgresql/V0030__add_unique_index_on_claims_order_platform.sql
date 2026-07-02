CREATE UNIQUE INDEX idx_claims_unique_order_platform
    ON claims (ecommerce_order_id, platform)
    WHERE is_deleted = false;
