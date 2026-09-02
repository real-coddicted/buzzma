-- Optional owner-funded reward on a campaign (ref #739). Nullable: absent type = no reward.
-- Only CASHBACK is supported for now; the paise column is set only when type = 'CASHBACK'.
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS additional_reward_type varchar(32);
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS additional_reward_cashback_paise numeric;
