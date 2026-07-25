# test migrations

Flyway migrations that apply only to the `test` environment.

Use this folder for test-specific seed data and config values that differ
from production. Do not copy prod values here speculatively — only what the
test environment actually needs to differ.

Naming: `V####__seed_<description>.sql`

All `updated_by` values in seed INSERTs must use the literal `'flyway'`.
