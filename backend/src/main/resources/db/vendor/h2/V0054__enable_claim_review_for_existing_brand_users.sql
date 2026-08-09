-- H2's CAST(jsonb_column AS VARCHAR) corrupts the value when the result is written back to a
-- column (verified: it double-encodes the JSON as a string), so jsonb_set-style partial updates
-- are not viable here. This local/dev-only vendor migration instead overwrites the settings
-- column with the known getBrandSettings() defaults (claimReviewEnabled now true), matching the
-- real per-role defaults rather than patching arbitrary existing content.
UPDATE user_settings us
SET settings = '{"dashboardTabEnabled":false,"campaignsTabEnabled":true,"assignmentsTabEnabled":false,"connectionsTabEnabled":true,"dealTabEnabled":false,"claimReviewEnabled":true,"ticketsTabEnabled":true,"feedbackTabEnabled":true,"settingsTabEnabled":true,"usersTabEnabled":false,"myPaymentsTabEnabled":false,"userPayoutsTabEnabled":false}'
FROM users u
WHERE us.user_id = u.id
  AND u.role = 'ROLE_BRAND';