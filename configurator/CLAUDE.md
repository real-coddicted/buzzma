# Configurator Service — CLAUDE.md

**Full design rationale lives in [`docs/architecture/config-service-design.md`](docs/architecture/config-service-design.md).
Treat every decision documented there as settled unless you surface a conflict explicitly.**

---

## What this module is

A standalone Spring Boot microservice that is the single source of truth for
runtime configuration and feature flags across all Buzzma services. Consuming
services embed a lightweight SDK (future step) that polls this service in the
background and reads config values from a local in-memory cache — application
code on the hot path never makes a network call to this service.

---

## Implementation phases

| Phase | Status    | Description |
|-------|-----------|-------------|
| 1     | ✅ Done   | Schema + Flyway structure — migration files under `db/migrations/` |
| 2     | ⏳ Next   | Config API service — Spring Boot app, read + write endpoints |
| 3     | Pending   | Java SDK / Spring Boot Starter |
| 4     | Pending   | Pilot integration into one consuming service |
| 5     | Pending   | Behavioral contract + OpenAPI spec for future language SDKs |

---

## Key constraints (not derivable from code)

- **No H2 — PostgreSQL is required even for local dev.** The schema uses three
  constructs H2 cannot replicate even in `MODE=PostgreSQL`:
  1. `CREATE TYPE ... AS ENUM` — H2 has no standalone enum types.
  2. `jsonb_typeof()` inside the `CHECK` constraint — H2 has no such function;
     dropping it in a local-only schema means the type-safety guarantee that
     exists in prod simply doesn't exist locally.
  3. PL/pgSQL trigger (`fn_config_entries_audit`) — H2's trigger mechanism is
     Java-class-based; there is no SQL-level equivalent. The trigger is
     architecturally load-bearing: it is what bumps the version on every write,
     which is what makes the SDK's delta-poll (`WHERE version > :since_version`)
     correct. Running without it locally tests fundamentally different behavior.

  **Local dev:** run against a local Postgres (e.g. `docker run -e
  POSTGRES_DB=buzzma -e POSTGRES_USER=buzzma -e POSTGRES_PASSWORD=buzzma -p
  5432:5432 postgres:16-alpine`) and use the `local` Spring profile
  (`application-local.yml`). Override `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` via
  `.env.local` if your local instance differs from the defaults.

  **Tests:** use Testcontainers (`postgres:16-alpine`) with `@ServiceConnection` —
  same pattern as `backend/`. The `test` Spring profile (`application-test.yml`)
  leaves datasource config as empty defaults, which `@ServiceConnection` overrides.

- **Separate Flyway history table.** This module shares the same Postgres
  instance as `backend/` (for now). Flyway is configured with
  `spring.flyway.table: flyway_schema_history_configurator` so migration
  histories never collide. Do not change this without coordinating with backend.

- **Environment isolation via separate DB instances (future).** Currently we
  share backend's DB. The eventual target is a dedicated Postgres instance for
  the configurator service per the design doc §4. When that happens, the
  `environment` column in `config_entries` changes from a safety-net annotation
  to a full isolation boundary.

- **This service is never on the application hot path.** Every design decision
  (polling, local cache, startup fallback) stems from this. Never add a
  synchronous config-read endpoint that consuming services call inline.

- **`updated_by` is always set from the authenticated caller identity**, never
  trusted from the request body. Flyway-authored rows use the literal `'flyway'`.

- **Delta poll uses a version sequence, not `updated_at` timestamps.** This is
  a deliberate correctness decision, not a style choice. PostgreSQL's `now()`
  returns the *transaction start time*, not the commit time. If a slow transaction
  A (started at T=100, commits at T=200) overlaps with a fast transaction B
  (started at T=150, commits at T=160), an SDK that polls at T=170 and stores
  `last_seen = 150` will never pick up transaction A's row — its `updated_at` is
  100, which is behind the watermark. The sequence (`config_version_seq`) is
  assigned inside the `BEFORE` trigger and is strictly monotonic, so
  `WHERE version > :last_version` has no equivalent gap. The low write frequency
  of this service makes the practical risk of timestamp-polling small, but the
  failure mode is silent data loss on the SDK side, which makes it not worth the
  simplicity tradeoff.

---

## Migration conventions

Migrations live in `src/main/resources/db/migrations/` with per-environment folders:

```
db/migrations/
  common/      # structural DDL — applies to every environment
  dev/         # dev-environment seed data
  staging/     # staging-environment seed data
  prod/        # production config values
```

When the Spring Boot runner is wired (Phase 2), Flyway locations will be set to
`classpath:db/migrations/common` + the environment-specific folder:

```yaml
spring:
  flyway:
    locations: classpath:db/migrations/common,classpath:db/migrations/${CONFIGURATOR_ENV:dev}
    table: flyway_schema_history_configurator
```

**Migration versioning** starts at V0001 independently of `backend/` (separate
Flyway history table). Use zero-padded four-digit version numbers: `V0001`, `V0002`, …

**Naming conventions:**
- `common/V####__<structural_description>.sql` — DDL only, no data
- `dev/V####__seed_<description>.sql`
- `staging/V####__seed_<description>.sql`
- `prod/V####__set_<namespace>_<key>.sql` — one logical config change per file

---

## Deferred / open items

Do not build these speculatively. Flag them to the user when they become relevant:

- Percentage-rollout / targeting rule engine (`evaluation_type`, `rules` columns
  are reserved extension points — see `V0001` comments)
- `namespace` / `environment` as FK-validated lookup tables (governance hardening)
- Flyway/Admin-API drift detection tooling
- Admin UI (CRUD, RBAC, approval workflows)
- Dead-flag lifecycle cleanup process
- Python SDK and other language SDKs (contract-first approach documented in design §8)
- Full infra isolation for prod DB (separate instance, credentials, network path)
