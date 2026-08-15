# Buzzma Communications

Spring Boot 3.3.4 · Java 21 · PostgreSQL · Redis · Gradle

Outbound-communications microservice for Buzzma. Sends email (with delivery-status tracking) and WhatsApp messages on behalf of the other Buzzma services.

---

## Tech stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Persistence | PostgreSQL + Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Queue | Redis (list + `BRPOP`, for the email outbox) |
| Mapping | MapStruct 1.5.5 |
| Code gen | Lombok |
| Security | Spring Security + JJWT |
| Email | Spring Mail (SMTP) |
| Build | Gradle |
| Code style | Spotless + Google Java Format + Checkstyle |

---

## Prerequisites

- Java 21
- PostgreSQL 15+
- Redis 6+
- Gradle (wrapper included)

---

## Running locally

This service can run as a single all-in-one process (default, easiest for local dev), or split into two independent instances — `api` and `worker` — that can be started, stopped, and scaled separately. See [API / worker split](#api--worker-split) below for why you'd want the split.

### Option A — docker-compose

```bash
cp .env.example .env   # fill in DB_URL, DB_USERNAME, DB_PASSWORD, REDIS_HOST, REDIS_PORT, etc.
docker compose up communications communications-worker
```

This starts two containers from the same image:
- `communications` — `SPRING_PROFILES_ACTIVE=api`, serves HTTP on `8083`
- `communications-worker` — `SPRING_PROFILES_ACTIVE=worker`, no HTTP port, just drains the email outbox queue

Both depend on `postgres` and `redis` being reachable — bring those up first if they aren't already running.

### Option B — two local `bootRun` processes (no Docker)

Needs a running Postgres and Redis reachable from your host. In two separate terminals:

```bash
# terminal 1 — API
export DB_URL=jdbc:postgresql://localhost:5432/buzzma_communications DB_USERNAME=buzzma DB_PASSWORD=buzzma
export FLYWAY_MIGRATOR_USER=buzzma FLYWAY_MIGRATOR_PASSWORD=buzzma
export REDIS_HOST=localhost REDIS_PORT=6379
export JWT_ACCESS_SECRET=change-me-access-secret-min-32-chars!!
export SPRING_PROFILES_ACTIVE=api
./gradlew bootRun

# terminal 2 — worker
export DB_URL=jdbc:postgresql://localhost:5432/buzzma_communications DB_USERNAME=buzzma DB_PASSWORD=buzzma
export FLYWAY_MIGRATOR_USER=buzzma FLYWAY_MIGRATOR_PASSWORD=buzzma
export REDIS_HOST=localhost REDIS_PORT=6379
export JWT_ACCESS_SECRET=change-me-access-secret-min-32-chars!!
export SPRING_PROFILES_ACTIVE=worker
./gradlew bootRun
```

Both processes run Flyway migrations on startup — that's fine, Flyway is idempotent. `FLYWAY_MIGRATOR_USER`/`FLYWAY_MIGRATOR_PASSWORD` default to blank, and Spring Boot's Flyway autoconfig treats a blank-but-set user as "use a dedicated datasource with these (empty) credentials", which fails with `FATAL: no PostgreSQL user name specified in startup packet` — always set them alongside `DB_USERNAME`/`DB_PASSWORD`.

Use a dedicated database (e.g. `buzzma_communications`) rather than pointing at a shared `buzzma` database used by other services — this service owns its own schema/migrations and shouldn't share a database with `backend`.

Once both are up:
1. `POST /api/email/messages` on the API instance → returns `202 Accepted` with status `EMAIL_STATUS_PENDING`.
2. The worker instance's logs show it popping the message off the Redis list and sending it.
3. `GET /api/email/messages/{requestId}` on the API instance → status flips to `EMAIL_STATUS_SENT` (or `EMAIL_STATUS_FAILED` with an error message).

### Option C — single process (no split)

Leave `SPRING_PROFILES_ACTIVE` unset. Both the HTTP API and the outbox worker run in the same process — simplest for quick local testing, but an outage in one takes down the other.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/buzzma DB_USERNAME=buzzma DB_PASSWORD=buzzma
export REDIS_HOST=localhost REDIS_PORT=6379
export JWT_ACCESS_SECRET=change-me-access-secret-min-32-chars!!
./gradlew bootRun
```

---

## Key commands

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Apply code formatting
./gradlew spotlessApply

# Check formatting and style (runs as part of check)
./gradlew check

# Compile only
./gradlew compileJava
```

---

## Module overview

All domain code lives under `com.coddicted.buzzma.communications.<module>`:

| Module | Description |
|---|---|
| `email` | Sending email and tracking delivery status |
| `whatsapp` | Sending/receiving WhatsApp messages via the Graph API webhook |
| `common` | Cross-cutting: `Auditable`, `AuditEntityListener` |
| `config` | App-wide configuration: security, JWT |
| `security` | JWT authentication filter/service |
| `util` | `FileUtils` and other shared helpers |

Layout within `email/`:

| Package | Contents |
|---|---|
| `email/controller` | `EmailMessageController` — HTTP layer. Excluded under the `worker` profile. |
| `email/service` | `EmailCommunicationLogService` — business logic, operates on entities only |
| `email/repository` | `EmailCommunicationLogRepository` — Spring Data JPA |
| `email/model` | `EmailCommunicationLog` entity, `EmailStatus` enum |
| `email/dto` | Request/response DTOs |
| `email/mapper` | MapStruct entity ↔ DTO mapping |
| `email/client` | `EmailClient` — thin wrapper around `JavaMailSender` |
| `email/config` | `EmailProperties` |
| `email/outbox` | `EmailOutboxPublisher`/`EmailOutboxWorker` — the Redis-backed async send queue. The worker is excluded under the `api` profile. |

---

## API / worker split

`POST /api/email/messages` doesn't send the email inline — it persists a `PENDING` log row, pushes a message onto a Redis list, and returns `202 Accepted` immediately. A separate `EmailOutboxWorker` thread pool (`app.email.outbox.worker-pool-size`, default `1`) does `BRPOP` on that list, sends the email, and updates the log to `SENT`/`FAILED`.

The API (`EmailMessageController`, `SecurityConfig`) and the worker (`EmailOutboxWorker`) are gated by `@Profile`, so they can run as independent processes:

| Profile | What runs |
|---|---|
| *(none)* | Both — single-instance mode, good for local dev |
| `api` | HTTP layer only, no outbox worker |
| `worker` | Outbox worker only — `spring.main.web-application-type: none`, no servlet container, no security filter chain |

This means an API outage doesn't stop pending emails from being sent, and a worker outage doesn't stop new requests from being accepted (they just queue up in Redis until a worker is available).

---

## Database migrations

Flyway migrations live in `src/main/resources/db/migration/`, naming convention `V{n}__description.sql`.

---

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/email/messages` | Queue an email to be sent. Returns `202 Accepted` with a `requestId`. |
| `GET` | `/api/email/messages/{requestId}` | Look up send status by request id. |
| `POST` | `/api/whatsapp/messages` | Send a WhatsApp message. |
| `GET`/`POST` | `/webhooks/whatsapp` | WhatsApp Graph API webhook (verification + inbound events). |
