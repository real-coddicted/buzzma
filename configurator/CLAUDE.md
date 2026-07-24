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
| 2     | ✅ Done   | Config API service — Spring Boot app, read + write endpoints |
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

- **Delta poll uses a change sequence, not `updated_at` timestamps.** This is
  a deliberate correctness decision, not a style choice. PostgreSQL's `now()`
  returns the *transaction start time*, not the commit time. If a slow transaction
  A (started at T=100, commits at T=200) overlaps with a fast transaction B
  (started at T=150, commits at T=160), an SDK that polls at T=170 and stores
  `last_seen = 150` will never pick up transaction A's row — its `updated_at` is
  100, which is behind the watermark. The sequence (`config_change_seq`) is
  assigned inside the `BEFORE` trigger and is strictly monotonic, so
  `WHERE change_seq > :last_change_seq` has no equivalent gap. The low write
  frequency of this service makes the practical risk of timestamp-polling small,
  but the failure mode is silent data loss on the SDK side, which makes it not
  worth the simplicity tradeoff.

---

## Migration conventions

Migrations live in `src/main/resources/db/migrations/` with per-environment folders:

```
db/migrations/
  common/      # structural DDL — applies to every environment
  local/       # local developer seed data
  staging/     # staging-environment seed data
  prod/        # production config values
```

Flyway locations are `classpath:db/migrations/common` + the environment-specific
folder, resolved from `CONFIGURATOR_ENV` (defaults to `local`):

```yaml
spring:
  flyway:
    locations: classpath:db/migrations/common,classpath:db/migrations/${CONFIGURATOR_ENV:local}
    table: flyway_schema_history_configurator
```

Services are started with `-Dspring.profiles.active=local` for local dev.
`CONFIGURATOR_ENV` is a separate concern: it controls which Flyway migration
folder runs, not which `application-*.yml` is loaded.

**Migration versioning** starts at V0001 independently of `backend/` (separate
Flyway history table). Use zero-padded four-digit version numbers: `V0001`, `V0002`, …

Flyway aggregates all configured locations into a single flat migration list, so
version numbers must be globally unique across folders. Use these reserved ranges:

| Folder    | Version range | Purpose                        |
|-----------|---------------|--------------------------------|
| `common/` | V0001–V0999   | Structural DDL, no data        |
| `local/`  | V1001–V1999   | Local dev seed data            |
| `staging/`| V2001–V2999   | Staging seed / smoke data      |
| `prod/`   | V3001–V3999   | Production config values       |

**Naming conventions:**
- `common/V####__<structural_description>.sql` — DDL only, no data
- `local/V####__seed_<description>.sql`
- `staging/V####__seed_<description>.sql`
- `prod/V####__set_<namespace>_<key>.sql` — one logical config change per file

**Tests** run with `CONFIGURATOR_ENV` defaulting to `local`, so Testcontainer runs
apply both `common/` and `local/` migrations — the same set that local dev uses.
This is intentional: keeping test and dev schema states in sync surfaces problems
with seed migrations before they reach CI.

---

## Phase 2 implementation notes

### Package layout

```
com.coddicted.buzzma.configurator/
  config/       SecurityConfig, ConfiguratorProperties
  converter/    AttributeConverters for the three PostgreSQL enum types
  controller/   ConfigController  (/v1/configs)
  dto/          Request/response DTOs
  entity/       ConfigEntry, ConfigEntryHistory
  enums/        ValueTypeEnum, EntryStatusEnum, EvaluationTypeEnum
  exception/    Typed exceptions + GlobalExceptionHandler
  repository/   ConfigEntryRepository, ConfigEntryHistoryRepository
  service/      ConfigService
```

### Auth — Phase 2 uses HTTP Basic

Write endpoints (`POST`, `PUT`, `DELETE`) require HTTP Basic authentication.
`spring.security.user.name/password` configure the credentials (via env vars
`ADMIN_USERNAME` / `ADMIN_PASSWORD`). The authenticated username becomes
`updated_by` in the DB — the controller passes `authentication.getName()` to
the service; the service never accepts `updatedBy` from the request body.

This is a placeholder that keeps the audit trail meaningful. Upgrading to
JWT + RBAC is a deferred item. Read endpoints (`GET`) are permit-all; network
isolation (§4 of the design doc) is the primary guard for SDK traffic.

### JPA ↔ PostgreSQL enum types

The schema uses `CREATE TYPE ... AS ENUM` with lowercase values (`'active'`,
`'deleted'`, etc.). Java enum constants are `ACTIVE`, `DELETED` (standard
style). Three `AttributeConverter` classes in `converter/` bridge the case gap.
`@Column(columnDefinition = "...")` names the PG type for reference; the
converter is what actually handles the mapping.

### Trigger-set fields — EntityManager.refresh()

`version`, `created_at`, and `updated_at` are all `insertable = false,
updatable = false` on the JPA entity. The DB trigger and column DEFAULTs set
them. After every `repository.save()` on a write path, the service does
`entityManager.flush()` + `entityManager.refresh()` to reload the
trigger-set values before returning them in the response. Without the refresh,
JPA's first-level cache returns stale values.

### In-process bulk-fetch cache

A `ConcurrentHashMap<String, CachedBulkFetch>` in `ConfigService` with a
configurable TTL (`configurator.bulk-fetch-cache-ttl-seconds`, default 5s)
collapses thundering-herd fleet restarts into one DB query. No Redis needed
at current scale. Cache is keyed on `namespace:environment`.

### `ddl-auto: none`

Set to `none` because PostgreSQL custom enum column types interfere with
Hibernate's validate pass. Flyway is the schema manager; JPA validate is
redundant and adds startup fragility from custom type metadata mismatches.

---

## Deferred / open items

Do not build these speculatively. Flag them to the user when they become relevant:

- **Auth upgrade** — replace HTTP Basic with JWT + RBAC. The audit trail is
  correct (username = `updated_by`) but a shared credential is not sufficient
  for multi-team or multi-environment write access in prod.
- Percentage-rollout / targeting rule engine (`evaluation_type`, `rules` columns
  are reserved extension points — see `V0001` comments)
- `namespace` / `environment` as FK-validated lookup tables (governance hardening)
- Flyway/Admin-API drift detection tooling
- Admin UI (CRUD, RBAC, approval workflows)
- Dead-flag lifecycle cleanup process
- Python SDK and other language SDKs (contract-first approach documented in design §8)
- Full infra isolation for prod DB (separate instance, credentials, network path)
