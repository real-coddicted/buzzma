# Buzzma Backend

Spring Boot 3.3.4 · Java 21 · PostgreSQL · Gradle

## Module structure

Each domain is a self-contained package under `com.coddicted.buzzma.<module>`:

```
<module>/
  api/          DTOs (request/response) — Jackson, Lombok @Value @Builder @Jacksonized
  controller/   REST controllers — mapping only, no business logic
  entity/       JPA entities + enums
  mapper/       MapStruct interfaces
  persistence/  Spring Data JPA repositories
  service/      Service interfaces (domain objects only, no DTOs)
  service/impl/ Service implementations
```

Cross-cutting code lives in `shared/`:
```
shared/common/      BaseCrudService, Auditable, AuditEntityListener
shared/exception/   Exception classes + GlobalExceptionHandler
shared/security/    JwtService, SecurityConfig, @CurrentUserId
shared/enums/       Global enums
```

## Layering rules

- **Controllers** own all DTO ↔ entity mapping via injected MapStruct mappers. They never contain business logic.
- **Services** (interface + impl) deal exclusively in domain entities (`Campaign`, `CampaignAssignment`, etc.) — no DTOs, no HTTP types.
- **Repositories** are Spring Data JPA interfaces. Custom queries use `@Query` with native SQL.

## Entities

- Annotate with `@Entity`, `@Table`, `@EntityListeners(AuditEntityListener.class)`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder(toBuilder = true)`.
- `@NoArgsConstructor` + `@AllArgsConstructor` are both required when using `@Builder` (JPA needs no-arg; builder needs all-arg).
- Primary key: `UUID` with `@Id @GeneratedValue @UuidGenerator`.
- Audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) via `Auditable` interface + `AuditEntityListener`.
- Soft delete via `isDeleted` boolean field — never hard-delete.
- Enums stored as `@Enumerated(EnumType.STRING)`. Enum values are prefixed: `CAMPAIGN_STATUS_DRAFT`, `CAMPAIGN_ACTION_PUBLISH`, etc.

## DTOs

- Immutable: `@Value @Builder @Jacksonized` (Lombok).
- Validation annotations (`@NotBlank`, `@Nullable`) on request DTOs.
- Suffix convention: `*RequestDto`, `*ResponseDto`.

## Mappers (MapStruct)

- `componentModel = "spring"`, `nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE`.
- Use `uses = OtherMapper.class` to compose mappers rather than duplicating conversion logic.
- Conversion helpers (`stringToUrl`, `stringToPlatform`) live in the mapper that owns the type.
- Always explicitly `ignore` audit fields and generated IDs in write mappers.

## Services

- Interface defines the contract with domain objects only.
- Impl extends `BaseCrudService` for common `mustFind` helper (throws `NotFoundException`).
- Impl is annotated `@Service`; write methods are `@Transactional`.
- Lookups that need to fail fast call `mustFind(repository, id, "Entity Name")` — never manually throw `NotFoundException` inline.

## Exception hierarchy

Throw these from services and domain components — never `ApiException` (that is HTTP-layer only):

| Exception | Mapped to |
|---|---|
| `NotFoundException` | 404 |
| `ForbiddenException` | 403 |
| `InvalidStateTransitionException` | 409 |
| `BusinessRuleViolationException` | 422 |

`GlobalExceptionHandler` translates all of the above to JSON error responses.

## State machine

`CampaignStateMachine` holds a static `Map<CampaignStatus, Set<CampaignStatus>>` transition table. Call `stateMachine.transition(entity, targetStatus)` — it mutates the entity's status or throws `InvalidStateTransitionException`.

## Security

- `@CurrentUserId` injects the authenticated user's `UUID` into controller parameters.
- Use it to pass the caller identity into service methods that need ownership checks.

## Code style

- Spotless with Google Java Format is enforced (`./gradlew spotlessApply`).
- Checkstyle uses `google_checks.xml` (test sources excluded).
- Annotation processor order in `build.gradle` must be: lombok → lombok-mapstruct-binding → mapstruct-processor.
- All method parameters and local variables that are never reassigned use `final`.
- No comments unless the WHY is non-obvious.
