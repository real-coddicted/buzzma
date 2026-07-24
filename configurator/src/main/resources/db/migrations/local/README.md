# local migrations

Flyway migrations that apply only to local developer environments.

Use this folder for:
- Seed data needed to exercise the service locally
- Test namespaces / feature flags for development

Naming: `V####__seed_<description>.sql`

All `updated_by` values in seed INSERTs must use the literal `'flyway'`.
