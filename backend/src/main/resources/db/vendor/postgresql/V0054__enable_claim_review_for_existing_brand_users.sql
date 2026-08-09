UPDATE user_settings us
SET settings = jsonb_set(us.settings, '{claimReviewEnabled}', 'true'::jsonb)
FROM users u
WHERE us.user_id = u.id
  AND u.role = 'ROLE_BRAND';