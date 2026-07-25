# prod migrations

Flyway migrations that apply only to the `prod` environment.

Use this folder for production config values. Each file should represent one
logical config change or a small cohesive group.

Naming: `V####__set_<namespace>_<key>.sql`

**Important:** Before authoring a new migration for a key that has ever been
changed via the Admin API (a live operational write), manually verify the live
value first. The Flyway history only reflects what Flyway wrote — it has no
visibility into Admin API changes. A new migration can silently undo a hotfix.
See the design doc §5 ("Flyway-owned values vs. Admin-API-owned operational toggles").

All `updated_by` values in seed INSERTs must use the literal `'flyway'`.
