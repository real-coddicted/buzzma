-- ClaimServiceImpl.createAppReviewClaim has no real order concept and hardcodes
-- ecommerce_order_id to 'NA'. The original index made 'NA' unique per platform globally, so only
-- one App Promotion claim could ever exist per platform across all users - every subsequent claim
-- hit a raw DataIntegrityViolationException / 500 on insert.
--
-- Recreated scoped to WHERE ecommerce_order_id <> 'NA', so it still guards against duplicate
-- claims on a real e-commerce order while allowing any number of 'NA' claims. Duplicate-claim
-- protection for App Promotion is already enforced at the deal level (one active deal per
-- campaign/owner), so this doesn't weaken any existing guarantee.
DROP INDEX idx_claims_unique_order_platform;

CREATE UNIQUE INDEX idx_claims_unique_order_platform
    ON claims (ecommerce_order_id, platform)
    WHERE is_deleted = false AND ecommerce_order_id <> 'NA';
