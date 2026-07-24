# Config & Feature Flag Service — Design Document

## How to use this document

This document is the complete record of an architecture design discussion for a
config/feature-flag service. It is written so that an engineer — or Claude Code —
picking this up has everything needed to continue implementation **as if they had
been part of the original design conversation**, not just a summary of conclusions.

Rules for picking this up:

- **Treat the decisions below as settled** unless a section explicitly says
  something is open/undecided. Don't silently re-litigate a decision that has
  documented rationale — if you think it's wrong, surface that to the user
  explicitly rather than deviating quietly.
- **Rationale matters as much as the decision.** Several sections include the
  alternatives that were considered and why they were rejected. When you hit an
  implementation choice this doc doesn't cover, reason from the same principles
  (scalability, maintainability, reuse; minimal DB hits; never block app startup
  on this service; prod isolated from non-prod) rather than defaulting to
  whatever's most common in a tutorial.
- **The "Open / Deferred Items" section at the end is the backlog.** Don't
  silently build solutions for those — flag them back to the user when they
  become relevant, the way they were deliberately deferred during design.
- **Update this document as implementation surfaces new decisions.** It's meant
  to stay the living source of truth, not a one-time handoff snapshot.
- Suggested repo placement: `docs/architecture/config-service.md`. The
  companion schema file (`config_service_schema.sql`) is a reference DDL, not
  yet split into Flyway-versioned migration files — see the Promotion Strategy
  section for how it should be restructured on the way into the repo.

---

## 1. Problem & Goals

Different microservices need feature flags and config values at runtime.
Environment variables and command-line parameters are painful to change without
a redeploy, so values should live in a database, be readable at runtime, and be
changeable without redeploying consuming services.

Guiding principles (in priority order, per the user's framing at the start of
this discussion): **scalability, maintainability, reuse.**

Concrete requirements that shaped every decision below:

- DB is the source of truth; changes must be picked up by running services
  without a restart.
- Actual DB hits must be minimal — services read from a cache, not the DB,
  on every access.
- Change propagation tolerance: **1-2 minutes is acceptable.** This single
  answer eliminated an entire category of complexity — see §5.
- Config service must never become a source of cascading failure for the
  services that depend on it.

Explicitly out of scope for the current phase (see §9 for how the schema
still leaves room for these later):

- Percentage-rollout / user-targeting rules ("we'll explore that separately")
- Secrets management (this is not a secrets manager; a config value could
  theoretically reference a secret's *name*, but never its value)

---

## 2. High-Level Architecture

```
   Admin UI / CI/CD (Flyway)
            |
            v  writes
      Config DB  (per-environment instance — see §4)
            ^
            |  reads (source of truth)
   Config API service  (stateless, one deployment per environment)
            ^
            |  bulk fetch on startup, delta poll every 30-60s (server-driven)
       SDK (embedded in each consuming service, local in-memory cache)
            ^
            |  synchronous in-memory read, no network call
        Application code
```

Key property, decided early and load-bearing for everything downstream:
**application code never makes a network call at request time.** It reads
from the SDK's local cache. The SDK's poll loop is a background concern,
completely decoupled from the request path.

### Why embedded SDK over a central service apps call live

Considered and rejected: a central Config API service that applications call
inline on every read (like a typical internal microservice call).

Rejected because:
- It adds a network hop + latency to every config read.
- It makes the Config API service a live dependency of every request path
  in every consuming service — an outage there cascades everywhere.
- It doesn't match the actual requirement: "cached values, minimal DB hits,
  pick up changes on next read" describes a background-refresh cache, not a
  live call-through.

The Config API service still exists and is the *only* thing that talks to the
DB — but SDKs call it on their own background schedule, never inline with an
application request.

---

## 3. Data Model

See `config_service_schema.sql` for the full DDL (PostgreSQL). Key decisions
and the reasoning behind each:

### Single global change sequence, not per-row versioning

One sequence (`config_change_seq`) is shared by every write across every
namespace/environment/key. This gives a strict global ordering of changes,
which is what makes the SDK's delta-poll query
(`WHERE change_seq > :since_change_seq`) both correct and cheap — a simple
indexed range scan, rather than comparing timestamps across rows (fragile
under clock skew) or tracking per-key sequences independently. It doubles as
an optimistic-concurrency token for the admin write path.

### Trigger-managed change_seq and audit, not application-managed

A DB trigger (`fn_config_entries_audit`) bumps `change_seq` and writes a row
to `config_entries_history` on every INSERT/UPDATE. This guarantees the audit
trail and change_seq bump happen even if a write comes through a path other
than the API service — a migration, a console session, a script. If this were
left to application code, bypassing that code path silently breaks both the
audit trail and the SDK's freshness signal.

### `value` as JSONB with a type-match CHECK constraint

One column holds boolean/string/number/json uniformly rather than four
nullable typed columns. A CHECK constraint validates the stored value
actually matches its declared `value_type` — DB-level defense in depth on
top of whatever validation the API service does.

### History is a separate, append-only table

Kept separate from `config_entries` so the table the SDK hits constantly
stays small and its indexes stay fast. History can grow indefinitely and is
queried independently (audit trail, admin UI "show history for this key").

### `evaluation_type` / `rules`: present but inert

`evaluation_type` currently only allows `'static'`. `rules JSONB` is
nullable and unused. These are reserved extension points for targeting/
rollout rules (see §9) — adding that capability later is an additive
`ALTER TYPE` + populating `rules`, not a schema rewrite. Do not build a rule
engine against these columns until that's explicitly picked back up.

Example shapes discussed for future rule-based evaluation (**not
implemented, illustrative only**):

```json
{ "type": "percentage", "percentage": 25, "salt": "new_checkout_flow", "default": false }
```
```json
{
  "type": "attribute_match",
  "conditions": [
    { "attribute": "user_tier", "operator": "in", "values": ["beta", "internal"] }
  ],
  "match": true,
  "default": false
}
```

### `namespace` column (renamed from `service_name`)

Renamed because a namespace may end up representing a team or logical
grouping, not strictly one microservice 1:1.

### `environment` column — role changed by the isolation decision (§4)

Originally designed as the isolation mechanism between environments within
one shared DB. That model was **rejected on security grounds** (see §4) —
the column's role changed from "query filter that provides isolation" to
"recorded metadata that provides a misconfiguration safety net." It's kept
even though it's redundant for filtering within a single-environment DB
instance, because:

- Each deployment of the Config API service knows which environment it's
  supposed to serve, and can assert that on startup against the rows it
  reads — e.g. refuse to start if it expects `test` but sees `prod` rows.
  Turns a silent wrong-DB-connection mistake into a loud startup failure.
- Preserves provenance if data is ever copied out of its original DB
  (backup restores, debugging snapshots).
- Cheap optionality — if lower environments are ever consolidated back into
  one DB, the column is already there; removing and re-adding it later is a
  real migration.

### Soft delete, not hard delete

`status` enum (`active` / `deleted`) rather than removing
rows. Supports a "dead flag" cleanup workflow and keeps history sane (a
deleted-then-recreated key doesn't lose its audit trail).

---

## 4. Environment Isolation

**Decision: prod runs on its own DB instance, its own credentials, its own
network path, and its own deployment of the Config API service — never
reachable from non-prod. Lower environments (dev, test, qa) may share a
single DB instance**, distinguished by the `environment` column as described
in §3.

### Why a single shared DB (environment as pure filter column) was rejected

Originally the schema used one DB for all environments with `environment` as
the isolation boundary. On review, this was identified as a real security
gap:

- A compromised/leaked dev credential could read or write prod data directly.
- A bug in the Config API service (missing `WHERE environment = ?`, a bad
  migration) could touch prod even though the mistake originated in a lower
  environment.
- Most compliance frameworks (SOC2, PCI-DSS, etc.) require prod isolated
  from non-prod as an infrastructure property, not a query-level one.
- Prod and dev have different uptime/blast-radius expectations that
  shouldn't be coupled to the same DB instance's maintenance windows.

A query filter is not a security boundary — anything with DB access can
ignore it. Physical/infrastructure isolation is.

### What "own deployment of the Config API service" means concretely

- Own DB connection string/credentials, least-privilege, never shared
  across tiers.
- Own network segment (VPC/subnet/security group) such that non-prod
  compute cannot reach the prod DB even if it had credentials.
- No single service instance holds connections to more than one
  environment's DB.

### Consequence: cross-environment tooling changes shape

"What's different between test and prod" and "promote this value" can no
longer be a single-DB query — see §5 for how promotion is actually handled
(Flyway, not the API service).

---

## 5. Promotion Strategy (Flyway)

**Decision: promotion is handled via Flyway migrations, checked into git,
with per-environment folders — not via a promotion feature inside the Config
API service.**

```
db/migrations/
  common/     -- structural DDL, applies to every environment
  dev/
  test/
  prod/
```

Each environment's Flyway run applies `common/` + its own folder as
`locations`. Since dev and test share one physical DB instance under the
isolation model in §4, they need **separate `flyway_schema_history` table
names** (Flyway supports overriding this) so their migration histories don't
collide against the same DB.

### Why the Config API service does not own promotion

Considered and rejected: an in-service "promote this value from test to
prod" operation. Rejected because it would require one service instance to
read from test's DB and write to prod's DB in a single call — which
directly violates the isolation just established in §4. Promotion has to be
an explicit, separately-authenticated, separately-audited action, not a
capability embedded in the read/write API.

### Flyway-owned values vs. Admin-API-owned operational toggles

Two independent writers exist on the same table: Flyway migrations (checked
in, reviewed, versioned) and the Admin API (live operational writes, e.g. an
on-call engineer flipping a kill-switch). This creates a real conflict
scenario, worth understanding precisely:

1. `prod/V12__set_max_retries.sql` sets `max_retries = 3`.
2. An incident happens; on-call hotfixes `max_retries = 10` via the Admin
   API, bypassing Flyway — a legitimate, expected use of that API.
3. Weeks later, someone authors `prod/V15__set_max_retries.sql` with
   `UPDATE ... SET value = '5'`, working from what the migration files say
   the value *should* be, with no visibility into the live hotfix.
4. `V15` runs, silently overwrites `10` with `5`, undoing the hotfix.

Note this is **not** Flyway re-running an old migration — versioned
migrations never re-apply once recorded in `flyway_schema_history`. It's
that Flyway's migration history only reflects what *Flyway* has done, not a
row's actual live history, so the two writers can drift apart silently.

**Decision on how to handle this: convention only, no systematic drift
check at this stage.** Keys that are ever touched live via the Admin API are
understood by the team to be "operational" and are not subsequently
overwritten by new Flyway migrations without the author manually checking
the live value first. A tooling-based drift check (compare live value to
last-migrated value before authoring a promotion migration) was discussed
as a future option but explicitly deferred — do not build it unless asked.

---

## 6. Config API Service

Stateless, thin layer in front of the DB. No business logic beyond
validation. One deployment per environment DB instance (§4) — never one
instance connecting to multiple environments' databases.

### Read endpoints (consumed by the SDK)

| Endpoint | Purpose |
|---|---|
| `GET /v1/configs?namespace=X&environment=Y` | Bulk fetch on SDK startup. Returns all active entries + `snapshot_change_seq` (max change_seq in the response). |
| `GET /v1/configs/delta?namespace=X&environment=Y&sinceChangeSeq=N` | Recurring poll. Returns rows with `change_seq > N`, **including** deleted ones so the SDK knows to drop them, not just what to add/update. Response also includes `poll_interval_seconds` (see below). |
| `GET /v1/configs/{namespace}/{environment}/{key}` | Single-key read. **SDKs never use this** — added specifically for admin UI / debugging so a detail view doesn't require fetching the whole namespace. |

**Server-driven poll interval — decided, not left to the SDK to hardcode.**
`poll_interval_seconds` in the delta-poll response is owned by the API
service (a per-environment setting, not computed per-request initially).
This means propagation speed can be tuned per environment without
redeploying every consuming service (e.g., tighten it during an incident).
SDK uses the server's value from the last successful response; falls back
to a hardcoded default (e.g. 60s) only before any poll has ever succeeded.

**Thundering-herd guard:** a short (few-second) in-process response cache
on the bulk-fetch endpoint, keyed on `namespace+environment`. When a fleet
restarts simultaneously and every instance bulk-fetches within the same
few seconds, this collapses that into effectively one DB query instead of
N. Small addition, not a new architectural layer (no Redis needed at
current scale — see §6.1 for when that might change).

### Write / admin endpoints

| Endpoint | Purpose |
|---|---|
| `POST /v1/configs` | Create a new entry. |
| `PUT /v1/configs/{id}` | Update value. Body includes `expected_change_seq`; service does compare-and-swap, returns `409 Conflict` if the row changed since the caller last read it. |
| `DELETE /v1/configs/{id}` | Soft delete (`status = deleted`), never a hard DB delete. |
| `GET /v1/configs/{id}/history` | Audit trail for one entry, for the admin UI. |
| `GET /v1/configs?namespace=X&environment=Y&search=...` | Paginated list/search, for the admin UI. |

**Important:** `updated_by` must be populated by the service from the
*authenticated caller's identity*, never trusted from the request body —
otherwise the audit trail is meaningless (anyone could claim to be anyone).

### Auth model

- **SDK → read API:** network-level isolation (§4) is doing most of the
  work already. Layer mTLS or workload identity on top so the service knows
  *which* namespace is calling — mainly to prevent one team's service from
  reading another team's flags, not because values are typically secret.
- **Admin/pipeline → write API:** real authentication + RBAC, since this
  path is what the audit trail's integrity depends on.

### 6.1 Not needed yet, but noted for when scale changes

- **Redis (L2 distributed cache)** in front of the DB — unnecessary at
  current scale (polling load is "one request per instance per ~45s", not
  per-application-request). Revisit if instance count grows enough that
  aggregate poll volume becomes a real DB load concern.
- **Push-based invalidation (pub/sub)** — deliberately skipped given the
  1-2 minute propagation tolerance from §1. Only reconsider if freshness
  requirements tighten.

---

## 7. SDK (Java / Spring Boot)

**Scope decision: Java only for now**, built as a proper **Spring Boot
Starter** (autoconfigured beans, `application.yml`-driven config, hooks into
Spring's own lifecycle) rather than a library the app wires up manually.
Other languages are addressed via a contract, not code — see §8.

### Public API — multi-namespace, decided explicitly

**Decision: a single SDK instance can hold multiple namespaces.** Each
namespace gets its own independent cache, version pointer, and poll cadence
underneath a shared client.

```java
ConfigClient client = ConfigClient.builder()
    .apiUrl("https://config-api.internal")
    .environment("prod")
    .register("checkout-service")
    .register("payments-service")
    .build();

NamespaceConfig checkout = client.forNamespace("checkout-service");
checkout.getBool("new_checkout_flow", false);
```

Every getter takes a **caller-provided local default** as a required
parameter — the last link in the fallback chain, living in application code
because only the application knows what "safe" means for that specific
flag if every other layer of caching has failed.

`onChange`/reactive callbacks were considered and **explicitly rejected for
now** — polling-and-reading-fresh-on-next-call is sufficient at the current
scope (no targeting, 1-2 min tolerance). This keeps the cache-swap
mechanism free of any listener registry or callback-ordering concerns.
Revisit only if a concrete reactive use case shows up.

### Internal components

- **Cache store:** `Map<namespace, NamespaceCache>`, each holding its own
  immutable `Map<key, ConfigEntry>` + `snapshot_change_seq`.
- **Fetcher:** wraps the two read endpoints (bulk fetch, delta poll).
  Owns retry/timeout logic only.
- **Poller:** one shared scheduler runs independent poll tasks per
  namespace, each on its own server-driven interval (namespaces can
  legitimately be told to poll at different rates).
- **Fallback chain**, walked in order at startup, per namespace
  independently: in-memory cache (if already running) → local disk
  snapshot → caller-provided defaults passed at the `get()` call site.

### Startup sequence

1. Bulk fetch, per namespace, in parallel.
2. Success → populate cache, write disk snapshot, start poller.
3. Failure → load last disk snapshot if one exists on this host; populate
   cache from it; still start the poller in the background so it can
   recover once the API is reachable — **the app is not blocked waiting**.
4. No snapshot either → cache stays empty for that namespace; every
   `get()` call falls through to the caller-provided default. Poller keeps
   retrying with backoff.

**Hard requirement: startup must never block indefinitely on the network.**
A config service outage must never be able to prevent an unrelated service
from starting. Bootstrap fetch is capped by a short timeout
(`bootstrap-timeout`, a few seconds); on timeout, fall through the chain
rather than hang.

### Poll loop details

- Interval = server-provided `poll_interval_seconds` + jitter (±10-20%) per
  namespace task, so a fleet that booted simultaneously doesn't settle into
  synchronized polling against the API service.
- On failure: exponential backoff up to a ceiling (e.g. 5 minutes), reset
  to the server-given interval on next success. **A failed poll never
  clears or degrades the cache** — it just means slightly stale data, an
  explicitly accepted tradeoff given the 1-2 min propagation requirement.
- On success: apply the delta (add/update changed keys, drop
  deleted ones), bump `snapshot_change_seq`, refresh the disk
  snapshot for that namespace.

### Concurrency — the atomic swap (the one place a bug would actually hurt)

The poller thread writes; arbitrary application threads read via `get()`
constantly, in parallel. Pattern: build a **new** immutable map from
old-map + delta, then swap a single reference
(`AtomicReference<Map<String, ConfigEntry>>.set(...)`). Readers see either
the fully-old or fully-new snapshot, never a half-updated one — no locks on
the read path, which matters because reads are the hot path.

### Disk snapshot

One JSON file per `namespace+environment` (not one shared file), written
after every successful fetch/poll, read only at startup. Purpose is narrow:
survive a restart when the Config API happens to be unreachable at that
exact moment, so the instance doesn't fall all the way back to hardcoded
defaults for values that were known and current moments ago.

### Local dev/test overrides

**Decided as a non-issue under the current isolation model.** Since dev,
test, and any developer sandbox each has its own DB instance (§4), a
developer can edit their own DB's rows directly, or point their local
instance at a disposable seeded DB. No SDK-level override mechanism is
needed. (This assumes no shared DB at any tier below test — if that
assumption changes, revisit.)

### Spring Boot Starter specifics

```yaml
config-sdk:
  api-url: https://config-api.internal
  environment: prod
  namespaces:
    - checkout-service
    - payments-service
  bootstrap-timeout: 3s
```

- **HTTP client: Spring's `RestClient`, not bare `java.net.http.HttpClient`**
  — decided after the SDK was already sketched with the JDK client, updated
  once it was clear the target apps are Spring Boot. Use a **dedicated
  `RestClient` bean**, not whatever `RestClient`/`RestTemplate` the host app
  already has configured — isolates the SDK from unrelated changes to the
  app's own HTTP/Jackson setup. Same reasoning for a **dedicated
  `ObjectMapper`** used only for API responses and disk snapshots.

  ```java
  @AutoConfiguration
  public class ConfigSdkAutoConfiguration {

      @Bean
      RestClient configRestClient(ConfigSdkProperties props) {
          return RestClient.builder()
              .baseUrl(props.getApiUrl())
              .requestFactory(ClientHttpRequestFactoryBuilder.detect()
                  .build(ClientHttpRequestFactorySettings.defaults()
                      .withConnectTimeout(Duration.ofSeconds(2))
                      .withReadTimeout(props.getBootstrapTimeout())))
              .messageConverters(converters -> converters.add(
                  new MappingJackson2HttpMessageConverter(configSdkObjectMapper())))
              .build();
      }
  }
  ```

- **Lifecycle:** implement `SmartLifecycle` so the bootstrap bulk-fetch
  (with its fallback chain) runs as part of Spring's startup phase, before
  the app reports ready, respecting `bootstrap-timeout`. Use an injected
  **`TaskScheduler`** (its own dedicated thread pool, not the shared
  `@EnableScheduling` one — the SDK's polling shouldn't starve or be
  starved by the app's own scheduled jobs) for per-namespace delta polling.
  `@PreDestroy`/lifecycle `stop()` cancels scheduled tasks on shutdown.

- **Observability (close to free given Spring Boot is already in play):**
  a custom `HealthIndicator` per namespace (cache age, last successful poll
  timestamp) surfaces automatically in Actuator's `/health`; Micrometer
  counters/gauges (cache hit ratio, poll failures, fallback-chain depth
  reached) if `micrometer-core` is on the classpath.

- **Distribution:** versioned artifact published to the internal
  Maven/Gradle repo, own SemVer independent of the Config API service's
  release cycle.

---

## 8. Extending Beyond Java (Python, and any future language)

**Decision: contract-first, not port-from-Java.** What must be
language-agnostic is the *behavioral contract* — not the Java
implementation itself. If a second SDK is built by reading the Java
source, the two will drift the first time either one changes
independently.

Before any second-language SDK is built, produce:

- An **OpenAPI spec** for the Config API's read endpoints (bulk fetch,
  delta poll, single-key read) — largely already implied by §6, just needs
  formalizing.
- A **behavioral spec document**: startup fallback order, the
  swap-not-mutate requirement, jitter range, backoff ceiling, and the disk
  snapshot JSON shape (include a `format_version` field so different
  language SDKs stay forward/backward compatible with each other's
  snapshot files).
- **Shared contract tests** that any SDK implementation must pass, rather
  than relying on one team reading another team's source as the spec.

### Python-specific notes (not yet built — sketch only)

- **HTTP:** `httpx` (explicit timeout handling; works for both sync and
  async).
- **Atomic swap:** build a new `dict`, reassign a single reference — under
  CPython's GIL, a single name rebind is effectively atomic for readers,
  giving the same "never see a half-updated map" property without explicit
  locks. **Note in code comments that this relies on CPython specifically**,
  not a language-level guarantee.
- **Poller:** a background `threading.Thread` sleep-loop for sync
  frameworks (Flask/Django); an `asyncio.Task` variant for async frameworks
  (FastAPI) — likely two thin variants sharing the same core cache/fallback
  logic.
- **Distribution:** PyPI package, same SemVer discipline as the Java
  artifact, versioned independently.

Non-Java, non-Python services in the meantime have no SDK — calling the
REST API directly with no caching is an accepted stopgap, not a design
decision to build around.

---

## 9. Open / Deferred Items

Nothing below should be built speculatively. Listed so they're not
forgotten, and so a partial solution doesn't get invented ad hoc during
implementation without the tradeoffs being revisited explicitly.

- **Targeting / rollout rule engine.** Schema has reserved hooks
  (`evaluation_type`, `rules`) but no evaluation logic, context object
  schema, rule precedence rules, or bucketing/hashing implementation exist
  yet. Explicitly deferred to a separate design pass.
- **`namespace`/`environment` as free text vs. FK-validated lookup
  tables.** Flagged as a possible governance hardening step (prevents
  typos / unregistered namespaces). No decision made either way.
- **Flyway/Admin-API drift detection tooling.** Convention-only for now
  (§5) — a systematic pre-promotion check comparing live vs. last-migrated
  value was discussed and explicitly not built.
- **Admin UI.** CRUD interface, RBAC for who can write to prod
  specifically, possible approval workflows. Endpoints exist (§6); no UI
  designed.
- **Dead-flag lifecycle.** Process for identifying and cleaning up flags
  nobody reads anymore. Not designed.
- **Secrets boundary.** Agreed this service is not a secrets manager, but
  no explicit line drawn for the case where a config value needs to
  reference a secret (e.g., a flag pointing at a secret's *name*).
- **Python SDK (and any other language).** Contract-first plan exists
  (§8); no implementation started.
- **Non-Java/Python service support.** Accepted stopgap is uncached direct
  REST calls; not something to build tooling around unless it becomes a
  real pain point.

---

## 10. Suggested Implementation Order

Not a rigid requirement, but reflects the dependency order of the pieces
above — each phase should land as its own branch/PR rather than one large
change:

1. **Schema + Flyway restructure.** Convert `config_service_schema.sql`
   into versioned migrations under `db/migrations/common/`, `dev/`,
   `test/`, `prod/` per §5. Stand up separate DB instances per §4 (or
   at minimum, separate schemas/credentials as a local approximation if
   full infra isolation isn't available yet).
2. **Config API service.** Read endpoints first (bulk fetch, delta poll,
   single-key), including the `poll_interval_seconds` field and the
   thundering-herd response cache. Write endpoints second, including
   optimistic concurrency and the authenticated `updated_by` requirement.
3. **Java SDK / Spring Boot Starter.** Single-namespace happy path first
   (fetch, cache, atomic swap, poll), then the fallback chain (disk
   snapshot, caller defaults, bounded startup timeout), then multi-namespace
   support, then the Actuator/Micrometer observability layer.
4. **Pilot integration.** Wire the starter into one real consuming
   service end-to-end before rolling out broadly.
5. **Contract docs for future SDKs** (§8) — worth doing once the Java SDK's
   behavior has stabilized through the pilot, so the spec reflects what
   was actually built and validated, not just what was designed on paper.
