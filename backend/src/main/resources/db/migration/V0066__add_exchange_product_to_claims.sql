-- Phase 1 (Exchange Deals, ref #726 / parent #382): schema-only groundwork.
-- Holds the single exchange product name the buyer picks on the order form.
-- Nullable for Phase 1 - existing claims have no value and nothing writes it yet.
-- The "mandatory for exchange campaigns" rule from #382 is a Phase 2 concern.
ALTER TABLE claims ADD COLUMN IF NOT EXISTS exchange_product varchar(255);
