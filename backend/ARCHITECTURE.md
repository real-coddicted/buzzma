# Architecture

## Module layout

The codebase is a single Spring Boot application organized into twelve domain modules under `src/main/java/com/mobo/`:

| Module          | Responsibility                                                                 |
|-----------------|--------------------------------------------------------------------------------|
| `brands`        | Brand entities, the brand-facing `/api/brand*` endpoints, brand-scoped queries |
| `agency`        | Agencies, agency-scoped lookups                                                |
| `mediator`      | Mediator profiles, pending connections, upstream-suspension filter             |
| `buyers`        | Shopper profiles                                                               |
| `catalog`       | Campaigns and deals                                                            |
| `orders`        | Orders, order items, order workflow, proofs, audit                             |
| `wallet`        | Wallets, transactions, payouts, settlement bookkeeping                         |
| `support`       | Tickets and ticket comments                                                    |
| `identity`      | Users, auth, invites, security questions, JWT filter, `MoboUserDetails`        |
| `admin`         | Audit logs, suspensions, system configs, ops dashboard, admin controllers      |
| `notifications` | Push subscriptions                                                             |
| `shared`        | Cross-cutting primitives: common base types, security config, exceptions, enums, health endpoint |

A thirteenth top-level class, `com.coddicted.buzzma.BuzzmaBackendApplication`, sits at the `com.mobo` root as the Spring Boot entry point.

## Package convention inside each module

```
com.mobo.<module>/
    api/            # public contract — DTOs, port interfaces
    web/            # @RestController classes
    service/        # service interfaces
    service/impl/   # service + port implementations
    persistence/    # @Entity and @Repository classes
    mapper/         # @Mapper (MapStruct) classes
    security/       # module-owned Spring Security coupling (identity, mediator only)
```

Modules omit sub-packages that would be empty.

## The "only `api` is public" rule

The sole enforced cross-module import contract:

> A class in module `X` may import from module `Y` **only** if the import target is in `com.mobo.Y.api.*` or in `com.mobo.shared.*`.

Everything else — `persistence`, `service`, `service.impl`, `web`, `mapper`, `security` — is module-internal. Cross-module access goes through a **port** declared in the owning module's `api` package and implemented in the owning module's `service/impl` package. Callers depend on the port interface and its DTOs, never on entities, repositories, or concrete services of another module.

This boundary is enforced by `src/test/java/com/mobo/architecture/ModuleBoundaryTest.java` (ArchUnit). The test has seven rules covering module residence, persistence/web/mapper placement, and cross-module reach-ins. Two framework-wiring classes are whitelisted because they cannot be expressed as DTO-only ports:

- `security.shared.com.coddicted.buzzma.SecurityConfig` — wires `JwtAuthenticationFilter` (identity) and `UpstreamSuspensionFilter` (mediator) into the filter chain.
- `security.mediator.com.coddicted.buzzma.UpstreamSuspensionFilter` — reads `MoboUserDetails` (identity) to enforce downstream-suspension propagation.

When a boundary violation is legitimate (new framework-wiring), add the fully-qualified class name to `CROSS_MODULE_IMPORT_WHITELIST` in the boundary test with a one-line justification. When it is illegitimate (a missed reach-in), introduce a port on the owning side.

## Monolith now, microservices later

The current deployable is a single Spring Boot jar. The module partition means that extracting any module into its own service requires only: (a) replacing the port's local `@Service` implementation with an HTTP/gRPC adapter, and (b) running the module's `persistence` package against its own datasource. No cross-module leakage through entities, repositories, or service classes blocks that split — because no such leakage exists today.

## Smoke test

`src/test/java/com/mobo/smoke/ApplicationSmokeTest.java` boots the full Spring context against a Testcontainers Postgres 16 instance, asserts `/api/health` returns 200 with the expected payload shape, and then hits one GET endpoint per domain module with a JWT-authenticated admin user. It runs on every `./gradlew test`. A regression in bean wiring, Flyway migrations, security filter chain, or any controller's routing will fail this test.
