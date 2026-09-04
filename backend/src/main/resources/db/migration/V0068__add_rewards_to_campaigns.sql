-- Optional owner-funded rewards on a campaign (ref #739), stored as a jsonb array so a campaign
-- can carry more than one reward at once (e.g. cashback and a future promo code together).
-- Each element is {"type": "CASHBACK", "value": "<paise>"}; only CASHBACK is supported for now.
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS rewards jsonb;
