CREATE INDEX idx_campaigns_type ON campaigns (type);
CREATE INDEX idx_campaigns_start_date ON campaigns (start_date);

-- H2 does not support function-based indexes; case-insensitive brand name search
-- is covered by the PostgreSQL migration in db/vendor/postgresql/.
SELECT 1;