# staging migrations

Flyway migrations that apply only to the `staging` environment.

Use this folder for staging-specific seed data and config values that differ
from production. Do not copy prod values here speculatively — only what staging
actually needs to differ.

Naming: `V####__seed_<description>.sql`

All `updated_by` values in seed INSERTs must use the literal `'flyway'`.
