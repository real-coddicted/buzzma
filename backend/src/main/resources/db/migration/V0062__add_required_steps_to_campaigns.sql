ALTER TABLE campaigns ADD COLUMN required_steps jsonb DEFAULT '[]';

UPDATE campaigns SET required_steps = '["ORDER", "RETURN_WINDOW"]'::jsonb
WHERE type IN ('CAMPAIGN_TYPE_ORDER', 'CAMPAIGN_TYPE_DISCOUNT');

UPDATE campaigns SET required_steps = '["ORDER", "RATING", "RETURN_WINDOW"]'::jsonb
WHERE type = 'CAMPAIGN_TYPE_RATING';

UPDATE campaigns SET required_steps = '["ORDER", "RATING", "REVIEW", "RETURN_WINDOW"]'::jsonb
WHERE type = 'CAMPAIGN_TYPE_REVIEW';
