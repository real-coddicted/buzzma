-- ============================================================
-- INVITE: drop role coupling, add multi-use counters
-- ============================================================

ALTER TABLE invites DROP COLUMN invitee_role;

ALTER TABLE invites ADD COLUMN max_use_count integer NOT NULL DEFAULT 1;
ALTER TABLE invites ADD COLUMN used_count    integer NOT NULL DEFAULT 0;