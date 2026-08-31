-- Phase 1 (Exchange Deals, ref #726 / parent #382): schema-only groundwork.
-- Nullable JSON array of selected exchange product names, mirroring the
-- assignments_draft jsonb "list of screenshots" pattern (V0017). No read/write
-- path touches this column in Phase 1.
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS exchange_products jsonb;
