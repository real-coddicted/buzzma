-- AppPromotionClaimCreationTemplate and PagePromotionClaimCreationTemplate have no real order
-- concept and hardcode ecommerce_order_id to 'NA'. The original index made 'NA' unique per
-- platform globally, so only one such claim could ever exist per platform. Exclude 'NA' rows so
-- the index still guards against duplicate claims on a real order.
DROP INDEX idx_claims_unique_order_platform;

CREATE UNIQUE INDEX idx_claims_unique_order_platform
    ON claims (ecommerce_order_id, platform)
    WHERE is_deleted = false AND ecommerce_order_id <> 'NA';
