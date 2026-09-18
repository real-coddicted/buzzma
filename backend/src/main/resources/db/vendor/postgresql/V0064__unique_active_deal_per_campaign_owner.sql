-- Enforce at most one active deal per (campaign, owner).
--
-- Duplicates arise when an assignment is published more than once
-- (CampaignAssignmentProcessor.publishAssignment has no idempotency guard): each call
-- inserts a fresh deals row. Two or more active rows for the same (campaign_id, owner_id)
-- then break the scalar deal-code sub-selects in
-- CampaignAssignmentRepository.findAssignmentSummaries and
-- DealRepository.findCodeByCampaignIdAndOwnerId
-- ("more than one row returned by a subquery" / NonUniqueResultException).
--
-- Any pre-existing duplicates must be resolved by hand before this migration runs
-- (soft-delete all but the earliest active deal per campaign_id + owner_id); otherwise
-- the index creation fails.

CREATE UNIQUE INDEX IF NOT EXISTS uq_deals_active_campaign_owner
    ON deals (campaign_id, owner_id)
    WHERE is_deleted = false;
