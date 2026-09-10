CREATE TABLE campaign_draft (
    id UUID PRIMARY KEY,
    code VARCHAR(16) UNIQUE,
    response_json JSONB NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Move existing draft-status campaigns into campaign_draft, storing a response-shaped JSON blob.
-- Assignments are not carried over (they were only ever a draft-of-a-draft in assignments_draft);
-- they must be re-entered before launch, same as any other draft created via the new endpoints.
INSERT INTO campaign_draft (id, code, response_json, created_by, updated_by, created_at, updated_at)
SELECT
    c.id,
    c.code,
    jsonb_build_object(
        'id', c.id,
        'code', c.code,
        'title', c.title,
        'ownerId', c.owner_id,
        'totalSlots', c.total_slots,
        'campaignType', c.type,
        'status', c.status,
        'startDate', c.start_date,
        'endDate', c.end_date,
        'productId', p.id,
        'productName', p.name,
        'productBrandName', p.brand_name,
        'productImageUrl', p.image_urls ->> 0,
        'productLink', p.product_link,
        'productPricePaise', p.price_paise,
        'platform', c.platform,
        'campaignPricePaise', c.campaign_price_paise,
        'returnWindowDays', c.return_window_days,
        'termsAndConditions', c.terms_and_conditions,
        'sellerName', c.seller_name,
        'requiredSteps', c.required_steps,
        'rewards', c.rewards,
        'exchangeProducts', c.exchange_products,
        'openToAll', c.open_to_all,
        'affiliateLinkAllowed', c.affiliate_link_allowed,
        'commissionToAllPaise', c.commission_to_all_paise,
        'assignments', '[]'::jsonb,
        'createdAt', c.created_at,
        'createdBy', c.created_by,
        'updatedAt', c.updated_at,
        'updatedBy', c.updated_by,
        'isDeleted', c.is_deleted
    ),
    c.created_by,
    c.updated_by,
    c.created_at,
    c.updated_at
FROM campaigns c
JOIN products p ON p.id = c.product_id
WHERE c.status = 'CAMPAIGN_STATUS_DRAFT';

DELETE FROM campaigns WHERE status = 'CAMPAIGN_STATUS_DRAFT';

ALTER TABLE campaigns DROP COLUMN assignments_draft;
