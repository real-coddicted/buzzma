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
shared/util/        Utility classes (CodeGenerator, DateTimeUtils, PasswordService)
```

## Layering rules

- **Controllers** own all DTO ↔ entity mapping via injected MapStruct mappers. They never contain business logic.
- **Services** (interface + impl) deal exclusively in domain entities (`Campaign`, `CampaignAssignment`, etc.) — no DTOs, no HTTP types.
- **Repositories** are Spring Data JPA interfaces. Custom queries use derived query methods or `@Query` with JPQL/native SQL.

## Entities

- Annotate with `@Entity`, `@Table`, `@EntityListeners(AuditEntityListener.class)`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder(toBuilder = true)`.
- `@NoArgsConstructor` + `@AllArgsConstructor` are both required when using `@Builder` (JPA needs no-arg; builder needs all-arg).
- Primary key: `UUID` with `@Id @GeneratedValue @UuidGenerator`.
- Audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) via `Auditable` interface + `AuditEntityListener`.
- Soft delete via `isDeleted` boolean field — never hard-delete.
- Enums stored as `@Enumerated(EnumType.STRING)`.

## Naming conventions

### Enum values

Enum values are prefixed with the entity/concept name in screaming snake case:

```java
public enum CampaignStatus {
  CAMPAIGN_STATUS_DRAFT,
  CAMPAIGN_STATUS_ACTIVE,
  CAMPAIGN_STATUS_PAUSED,
  CAMPAIGN_STATUS_CLOSED,
  CAMPAIGN_STATUS_COMPLETED
}

public enum CampaignAction {
  CAMPAIGN_ACTION_PUBLISH,
  CAMPAIGN_ACTION_PAUSE,
  CAMPAIGN_ACTION_CLOSE
}
```

### Classes and files

- Entities: `Campaign`, `CampaignAssignment`
- DTOs: `CampaignRequestDto`, `CampaignResponseDto`, `AssigneeDto` — always singular, matching the entity name
- Mappers: `CampaignMapper`, `ProductMapper`
- Services: `CampaignService` (interface), `CampaignServiceImpl` (impl)
- Repositories: `CampaignRepository`
- Controllers: `CampaignController`

### Fields

- Plain-text input fields use the semantic name without implementation detail: `answer` not `answerHash`.
- Stored hash fields use the `*Hash` suffix: `answerHash`, `passwordHash`.
- Boolean soft-delete field: `isDeleted` (not `deleted`).
- Timestamp fields: `createdAt`, `updatedAt` (not `created`, `createDate`).
- Actor fields: `createdBy`, `updatedBy` (UUID of the user who performed the action).

### Repository methods

Soft-delete aware derived queries follow this pattern:

```java
List<Campaign> findAllByIsDeletedFalse();
Optional<Campaign> findByIdAndIsDeletedFalse(UUID id);
Optional<Invite> findByCodeAndIsDeletedFalse(String code);
Optional<Invite> findByCodeAndRoleAndIsDeletedFalse(String code, UserRole role);
```

Never include deleted records by default — always suffix with `AndIsDeletedFalse` on any finder that returns live records.

## Entity update pattern (immutability via toBuilder)

Never mutate a loaded entity directly. Use `toBuilder()` to produce a modified copy, then save that copy:

```java
// soft delete
final Campaign updated = existing.toBuilder()
    .isDeleted(true)
    .updatedBy(requesterId)
    .build();
return campaignRepository.save(updated);

// field update
final Invite consumed = existing.toBuilder()
    .status(INVITE_STATUS_USED)
    .updatedBy(requesterId)
    .build();
return inviteRepository.save(consumed);

// copy to a new record (clear id so JPA inserts)
final Campaign copy = src.toBuilder()
    .id(null)
    .status(CampaignStatus.CAMPAIGN_STATUS_DRAFT)
    .createdAt(null)
    .updatedAt(null)
    .createdBy(requesterId)
    .updatedBy(requesterId)
    .build();
return campaignRepository.save(copy);
```

Rules:
- Always clear `id`, `createdAt`, `updatedAt` when copying to a new record — `AuditEntityListener` will populate the timestamps.
- Set `createdBy` / `updatedBy` to `requesterId` on every write.
- Apply business logic transformations (hashing, code generation) in `toBuilder()` before save — never pre-mutate the loaded entity.
- The only exception is `CampaignStateMachine.transition()`, which calls `setStatus()` directly on the entity before the caller saves it — that is intentional and scoped to the state machine only.

## DTOs

- Immutable: `@Value @Builder @Jacksonized` (Lombok).
- Validation annotations (`@NotBlank`, `@Nullable`) on request DTOs.
- Suffix convention: `*RequestDto`, `*ResponseDto`.
- Never include sensitive fields (`*Hash`, `*Secret`) in response DTOs.

## Mappers (MapStruct)

- `componentModel = "spring"`, `nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE`.
- Use `uses = OtherMapper.class` to compose mappers rather than duplicating conversion logic. When `uses` is declared, MapStruct auto-delegates matching type conversions — no explicit `@Mapping` needed for those fields.
- Conversion helpers (`stringToUrl`, `stringToPlatform`) live in the mapper that owns the type.
- Mappers are pure structural transformations: no business logic, no side effects, no service/component injection.
- Field name mismatches between DTO and entity (e.g. `answer` → `answerHash`) are handled with explicit `@Mapping(source = "answer", target = "answerHash")`.
- Always explicitly `@Mapping(target = "id", ignore = true)` and ignore audit fields in write mappers.

## Services

- Interface defines the contract with domain objects only.
- Impl extends `BaseCrudService` for common `mustFind` helper (throws `NotFoundException`).
- Impl is annotated `@Service`; write methods are `@Transactional`.
- Read-only methods (list, get) are annotated `@Transactional(readOnly = true)`.
- Lookups that need to fail fast call `mustFind(repository, id, "Entity Name")` — never manually throw `NotFoundException` inline.
- Business rule checks throw application-layer exceptions (`ForbiddenException`, `BusinessRuleViolationException`, `InvalidStateTransitionException`), never `ApiException`.

## Controllers

- Annotated with `@RestController`, `@RequestMapping("/api/v1/<resource>")`.
- Inject the MapStruct mapper and the service — nothing else.
- `@CurrentUserId final UUID requesterId` is always the **first parameter** on any method that needs the caller identity:
  ```java
  public InvitesResponseDto create(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final InvitesRequestDto request) {

  public void delete(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id) {
  ```
- Use `@ResponseStatus` for non-200 success codes:
  - `@ResponseStatus(HttpStatus.CREATED)` on create endpoints.
  - `@ResponseStatus(HttpStatus.NO_CONTENT)` on delete endpoints.
- Map request DTO → entity with mapper, call service, map result → response DTO. No logic in between.

## Exception hierarchy

Throw these from services and domain components — never `ApiException` (that is HTTP-layer only):

| Exception | Mapped to |
|---|---|
| `NotFoundException` | 404 |
| `ForbiddenException` | 403 |
| `InvalidStateTransitionException` | 409 |
| `BusinessRuleViolationException` | 422 |

`GlobalExceptionHandler` translates all of the above to JSON error responses.

## Logging

Use manual SLF4J — do not use Lombok `@Slf4j`:

```java
private static final Logger LOGGER = LoggerFactory.getLogger(InviteServiceImpl.class);
```

The field must be named `LOG` (uppercase) to satisfy the Checkstyle `ConstantName` rule.

Log levels:
- `log.debug(...)` — method entry, successful path milestones.
- `log.warn(...)` — business rule failures (invalid invite, wrong role, expired code). Always include the key identifier (e.g. code, id) and the reason.
- `log.error(...)` — unexpected exceptions (in `GlobalExceptionHandler`).

Example pattern from `InviteServiceImpl.verify()`:

```java
if (invite.isDeleted()) {
  log.warn("Invite {} is deleted", invite.getCode());
  return false;
}
if (invite.getStatus() != INVITE_STATUS_ACTIVE) {
  log.warn("Invite {} has status {}", invite.getCode(), invite.getStatus());
  return false;
}
```

## State machine

`CampaignStateMachine` holds a static `Map<CampaignStatus, Set<CampaignStatus>>` transition table. Call `stateMachine.transition(entity, targetStatus)` — it mutates the entity's status or throws `InvalidStateTransitionException`.

Capture any state-dependent flags **before** calling `transition()` — after the call the entity's status has already changed:

```java
// capture BEFORE transition
final boolean isPublish = campaign.getStatus() != CAMPAIGN_STATUS_ACTIVE
    && target == CAMPAIGN_STATUS_ACTIVE;
stateMachine.transition(campaign, target);
if (isPublish) { ... }
```

## Switch expressions

Prefer switch expressions over switch statements for exhaustive enum matching — no `default` branch needed when all values are handled:

```java
final CampaignStatus target = switch (action) {
  case CAMPAIGN_ACTION_PUBLISH -> CAMPAIGN_STATUS_ACTIVE;
  case CAMPAIGN_ACTION_PAUSE   -> CAMPAIGN_STATUS_PAUSED;
  case CAMPAIGN_ACTION_CLOSE   -> CAMPAIGN_STATUS_CLOSED;
};
```

## Utilities

- `CodeGenerator.generateHumanCode("PREFIX")` — generates a human-readable unique code (e.g. `INV-ABCD1234`). Use for invite codes, coupon codes, and any user-facing identifiers.
- `DateTimeUtils.toLocalDate(int date)` — parses an integer in `YYYYMMDD` format to `LocalDate` using `DateTimeFormatter.BASIC_ISO_DATE`. Use for date fields stored as `int` (e.g. `validTo`).
- `PasswordService.hashPassword(String)` / `verifyPassword(String, String)` — BCrypt hash and verify. Hashing always happens in the service layer via `toBuilder()`, never in a mapper.

## Security

- `@CurrentUserId` injects the authenticated user's `UUID` into controller parameters.
- Use it to pass the caller identity into service methods that need ownership checks.

## Code style

- Spotless with Google Java Format is enforced (`./gradlew spotlessApply`).
- Checkstyle uses `google_checks.xml` (test sources excluded).
- Annotation processor order in `build.gradle` must be: lombok → lombok-mapstruct-binding → mapstruct-processor.
- All method parameters and local variables that are never reassigned use `final`.
- No wildcard imports (`import foo.*`) — always use explicit imports, including `static` imports and `org.mapstruct.*`, `jakarta.persistence.*`, `lombok.*`.
- Always use braces on `if`/`else`/`for`/`while` — even single-line bodies.
- No comments unless the WHY is non-obvious.
