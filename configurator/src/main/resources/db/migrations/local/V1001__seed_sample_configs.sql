-- Sample config entries for local dev testing.
-- Covers all four value types (boolean, string, number, json).
-- updated_by = 'flyway' per convention for migration-authored rows.
-- The DB trigger handles version, created_at, and updated_at automatically.

INSERT INTO config_entries (namespace, environment, key, value_type, value, description, owner, updated_by)
VALUES
    -- Feature flags (boolean)
    ('buzzma-backend', 'dev', 'new_claim_review_ui_enabled',
     'boolean', 'true',
     'Enables the redesigned claim review UI for mediators', 'platform-team', 'flyway'),

    ('buzzma-backend', 'dev', 'campaign_slot_booking_enabled',
     'boolean', 'true',
     'Kill-switch to disable slot booking across all campaign types', 'campaigns-team', 'flyway'),

    -- String configs
    ('buzzma-backend', 'dev', 'support_contact_email',
     'string', '"support@buzzma.dev"',
     'Support email shown to buyers in error and confirmation messages', 'platform-team', 'flyway'),

    ('buzzma-backend', 'dev', 'gemini_model_override',
     'string', '"gemini-2.0-flash"',
     'Overrides the Gemini model used for screenshot extraction; empty string means use app default', 'ml-team', 'flyway'),

    -- Number configs
    ('buzzma-backend', 'dev', 'max_screenshot_upload_size_mb',
     'number', '10',
     'Maximum allowed size in MB for a single claim screenshot upload', 'platform-team', 'flyway'),

    ('buzzma-backend', 'dev', 'claim_review_sla_hours',
     'number', '48',
     'Target SLA in hours for mediators to complete a claim review', 'operations-team', 'flyway'),

    -- JSON configs
    ('buzzma-backend', 'dev', 'score_calculation_weights',
     'json', '{"review_quality": 0.4, "delivery_speed": 0.3, "screenshot_clarity": 0.3}',
     'Weights used in the claim score calculation model; must sum to 1.0', 'scoring-team', 'flyway'),

    ('buzzma-backend', 'dev', 'rate_limit_config',
     'json', '{"auth_capacity": 100, "auth_refill_tokens": 100, "user_capacity": 50}',
     'Rate limiter tuning for auth and per-user endpoints', 'platform-team', 'flyway');
