# Buzzma Backend

Spring Boot 3.3.4 · Java 21 · PostgreSQL · Gradle

Buzzma is a cashback/review campaign platform. Brands run campaigns, buyers claim deals, submit purchase and review proof, and earn rewards. This service provides the REST API consumed by the mobile and web frontends.

---

## Tech stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Persistence | PostgreSQL + Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Mapping | MapStruct 1.5.5 |
| Code gen | Lombok |
| Security | Spring Security + JJWT |
| Storage | AWS S3 / Garage (S3-compatible) / local |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle |
| Code style | Spotless + Google Java Format + Checkstyle |

---

## Prerequisites

- Java 21
- PostgreSQL 15+
- Gradle (wrapper included)

---

## Running locally

1. Copy `src/main/resources/application-local.yml` and fill in your credentials. The main `application.yml` is gitignored and must exist with at minimum:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/buzzma
    username: <user>
    password: <password>
  flyway:
    enabled: true
```

2. Run the application:

```bash
./gradlew bootRun
```

3. Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

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

All domain code lives under `com.coddicted.buzzma.<module>`:

| Module | Description |
|---|---|
| `campaign` | Campaigns, products, deals, assignments, commissions |
| `claim` | Buyer claim workflow — purchase proof, review proof, return proof, verification |
| `config` | App-wide configuration: OpenAPI, request logging, dev data seeding |
| `feedback` | User feedback submissions |
| `identity` | Authentication (JWT), user management, security questions |
| `settings` | User and organization settings |
| `shared` | Cross-cutting: base classes, exceptions, security, enums, utilities |
| `storage` | File storage abstraction — S3/Garage and local implementations |
| `support` | Support tickets, categories, comments, attachments |

Cross-cutting code in `shared/`:

| Package | Contents |
|---|---|
| `shared/common/` | `BaseCrudService`, `Auditable`, `AuditEntityListener` |
| `shared/exception/` | Exception classes + `GlobalExceptionHandler` |
| `shared/security/` | `JwtService`, `SecurityConfig`, `@CurrentUserId` |
| `shared/enums/` | Enums shared across multiple modules |
| `shared/util/` | `CodeGenerator`, `DateTimeUtils`, `PasswordService` |

---

## Database migrations

Flyway migrations live in `src/main/resources/db/migration/`.

Naming convention: `V{4-digit-sequence}__description.sql` — e.g. `V0001__baseline.sql`, `V0002__add_claims.sql`.

---

## API versioning

All endpoints are prefixed with `/api/v1/`.

---

## Architecture overview

The codebase follows a strict layered architecture within each module:

```
Controller  →  (mapper)  →  Service  →  Repository
    ↕                          ↕
  DTOs                      Entities
```

- **Controllers** handle HTTP: parse requests, call services, map results to DTOs. No business logic.
- **Services** own all business logic and operate only on domain entities.
- **Repositories** are Spring Data JPA interfaces — no business logic, no DTO references.
- **Mappers** (MapStruct) are pure structural transformations — no logic, no side effects.
- **Processors** (`@Component`) orchestrate multi-step operations that span multiple services, returning DTOs. Used when a single controller action requires coordinating several service calls.
