-- The campaign cashback (ref #739) folded into both receivable amounts, kept as a separate
-- column for traceability and deterministic recompute. Null / 0 when the campaign has no cashback.
ALTER TABLE claim_accountings ADD COLUMN IF NOT EXISTS additional_reward_cashback_paise numeric;
