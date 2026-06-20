DROP INDEX IF EXISTS idx_users_mobile;
CREATE UNIQUE INDEX idx_users_mobile ON users (mobile);