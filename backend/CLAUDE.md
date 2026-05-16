# Buzzma Backend

Spring Boot 3.3.4 · Java 21 · PostgreSQL · Gradle

## Module structure

Each domain is a self-contained package under `com.coddicted.buzzma.<module>`:

```
<module>/
  controller/   REST controllers — mapping only, no business logic
  dto/          DTOs (request/response) — Jackson, Lombok @Value @Builder @Jacksonized
  entity/       JPA entities + enums local to the module
  mapper/       MapStruct interfaces
  model/        Composite domain objects (non-JPA, aggregates multiple entities)
  persistence/  Spring Data JPA repositories
  predicate/    Predicate<T> implementations for business rule checks
  processor/    @Component orchestrators for multi-step operations (rare; see below)
  service/      Service interfaces (domain objects only, no DTOs)
  service/impl/ Service implementations
  util/         Module-specific utility classes (final, private constructor, static methods)
```

Not every module has all sub-packages — only create what the module needs.

Cross-cutting code lives in `shared/`:
```
shared/common/      BaseCrudService, Auditable, AuditEntityListener
shared/exception/   Exception classes + GlobalExceptionHandler
shared/security/    JwtService, SecurityConfig, @CurrentUserId
shared/enums/       Enums shared across multiple modules
shared/util/        Utility classes (CodeGenerator, DateTimeUtils, PasswordService)
```

Top-level modules alongside the domain modules:
```
config/     App-wide Spring configuration (@Configuration classes, filters, dev seeders)
storage/    File storage abstraction — StorageService interface + S3/local implementations
```

## Modules

| Module | Description |
|---|---|
| `campaign` | Campaigns, products, deals, assignments, commissions, state machine |
| `claim` | Buyer claim workflow — purchase proof, review, return, screenshot verification |
| `config` | OpenAPI, request logging, DevDataSeeder |
| `feedback` | User feedback submissions |
| `identity` | Auth (JWT), user CRUD, security questions |
| `settings` | User/org settings with JSONB storage |
| `shared` | Cross-cutting: base classes, exceptions, security, global enums, utilities |
| `storage` | `StorageService` abstraction; Garage (S3-compatible) and local impls |
| `support` | Support tickets, categories, sub-categories, comments, attachments |

## Layering rules

- **Controllers** own all DTO ↔ entity mapping via injected MapStruct mappers. They never contain business logic.
- **Services** (interface + impl) deal exclusively in domain entities (`Campaign`, `CampaignAssignment`, etc.) — no DTOs, no HTTP types.
- **Repositories** are Spring Data JPA interfaces. Custom queries use derived query methods or `@Query` with JPQL/native SQL.
- **Processors** are `@Component` beans used when a controller action requires coordinating multiple service calls or producing a DTO that requires multi-step assembly. They may return DTOs. Use sparingly — prefer keeping logic in services when possible.
- **Model objects** (in `model/`) are plain Java classes (Lombok-annotated, non-JPA) that represent a composed view of multiple entities. Use them when a service needs to return aggregated data that doesn't map cleanly to a single entity.

## Entities

- Annotate with `@Entity`, `@Table`, `@EntityListeners(AuditEntityListener.class)`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder(toBuilder = true)`.
- `@NoArgsConstructor` + `@AllArgsConstructor` are both required when using `@Builder` (JPA needs no-arg; builder needs all-arg).
- Primary key: `UUID` with `@Id @GeneratedValue @UuidGenerator`.
- Audit fields (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`) via `Auditable` interface + `AuditEntityListener`.
- Soft delete via `isDeleted` boolean field — never hard-delete.
- Enums stored as `@Enumerated(EnumType.STRING)`.
- Use `org.springframework.transaction.annotation.Transactional` — not `jakarta.transaction.Transactional`.

### JSONB columns

Store structured sub-objects as PostgreSQL `jsonb` using `@JdbcTypeCode(SqlTypes.JSON)`:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "settings", columnDefinition = "jsonb")
private Settings settings;
```

The embedded value type uses the same annotations as a DTO — immutable, builder-friendly, Jackson-deserializable:

```java
@Value
@Builder
@Jacksonized
public class Settings {
    boolean isDashboardTabEnabled;
    boolean isCampaignsTabEnabled;
    // ...
}
```

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

public enum Platform {
  PLATFORM_AMAZON,
  PLATFORM_FLIPKART,
  PLATFORM_NYKAA,
  PLATFORM_MYNTRA
}
```

Exception: workflow/status enums in `shared/enums/` that describe cross-module states may use un-prefixed values when the type name already provides sufficient context (e.g. `ClaimWorkflowStatus.ORDERED`).

### Classes and files

- Entities: `Campaign`, `CampaignAssignment`
- DTOs: `CampaignRequestDto`, `CampaignResponseDto` — always singular, matching the entity name
- Mappers: `CampaignMapper`, `ProductMapper`
- Services: `CampaignService` (interface), `CampaignServiceImpl` (impl)
- Repositories: `CampaignRepository`
- Controllers: `CampaignController`
- Model objects: `Assignment` (in `campaign/model/`)
- Processors: `CampaignProcessor` (in `campaign/processor/`)

### Fields

- Plain-text input fields use the semantic name without implementation detail: `answer` not `answerHash`.
- Stored hash fields use the `*Hash` suffix: `answerHash`, `passwordHash`.
- Boolean soft-delete field: `isDeleted` (not `deleted`).
- Timestamp fields: `createdAt`, `updatedAt` (not `created`, `createDate`).
- Actor fields: `createdBy`, `updatedBy` (UUID of the user who performed the action).
- Money fields: suffix `*Paise` for amounts stored as integer paisa (100 paise = ₹1).

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
- Paged list responses: `Paged*ResponseDto` containing `List<*ResponseDto> items`, `long total`, `int page`, `int totalPages`.
- Never include sensitive fields (`*Hash`, `*Secret`) in response DTOs.

## Mappers (MapStruct)

- `componentModel = "spring"`, `nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE`.
- Use `uses = OtherMapper.class` to compose mappers rather than duplicating conversion logic. When `uses` is declared, MapStruct auto-delegates matching type conversions — no explicit `@Mapping` needed for those fields.
- Conversion helpers (`urlToString`, `stringToPlatform`) live in the mapper that owns the type, annotated `@Named`.
- Mappers are pure structural transformations: no business logic, no side effects, no service/component injection.
- Field name mismatches between DTO and entity (e.g. `answer` → `answerHash`) are handled with explicit `@Mapping(source = "answer", target = "answerHash")`.
- Always explicitly `@Mapping(target = "id", ignore = true)` and ignore all audit fields in write mappers (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `isDeleted`).
- Multi-parameter `toResponse` methods are supported: `ClaimResponseDto toResponse(Claim claim, Campaign campaign, List<ClaimScreenshot> screenshots)`. MapStruct resolves source paths using the parameter name as prefix.
- **Boolean `is`-prefix stripping in source paths**: MapStruct strips the `is` prefix from boolean property names when resolving source paths. A field named `isDashboardTabEnabled` must be referenced as `dashboardTabEnabled` in `source`, even though the full name is used in `target`:
  ```java
  @Mapping(source = "settings.dashboardTabEnabled", target = "isDashboardTabEnabled")
  ```
- **Flattening nested objects**: when an entity holds an embedded value object (e.g. `UserSettings.settings`) that maps to a flat DTO, declare explicit `@Mapping` for each nested field using dot notation in `source`.

## Services

- Interface defines the contract with domain objects only.
- Impl extends `BaseCrudService` for common `mustFind` helper (throws `NotFoundException`).
- Impl is annotated `@Service`; write methods are `@Transactional`.
- Read-only methods (list, get) are annotated `@Transactional(readOnly = true)`.
- Lookups that need to fail fast call `mustFind(repository, id, "Entity Name")` — never manually throw `NotFoundException` inline.
- Business rule checks throw application-layer exceptions (`ForbiddenException`, `BusinessRuleViolationException`, `InvalidStateTransitionException`), never `ApiException`.

## Controllers

- Annotated with `@RestController`, `@RequestMapping("/api/v1/<resource>")`.
- Inject the MapStruct mapper and the service — nothing else. If a processor exists for the module, inject that instead of the service.
- `@CurrentUserId final UUID requesterId` is always the **first parameter** on any method that needs the caller identity:
  ```java
  public ClaimResponseDto create(
      @CurrentUserId final UUID requesterId,
      @Valid final ClaimRequestDto request) {

  public void delete(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id) {
  ```
- Use `@ResponseStatus` for non-200 success codes:
  - `@ResponseStatus(HttpStatus.CREATED)` on create endpoints.
  - `@ResponseStatus(HttpStatus.NO_CONTENT)` on delete endpoints.
- Map request DTO → entity with mapper, call service, map result → response DTO. No logic in between.

## Processors

Use a `@Component` processor when a controller action requires coordinating multiple services or assembling a DTO from multiple sources and that logic doesn't belong in any single service. Processors live in `<module>/processor/`.

```java
@Component
public class CampaignProcessor {

  private final CampaignService campaignService;
  private final ProductProcessor productProcessor;
  private final CampaignMapper campaignMapper;

  @Transactional
  public CampaignResponseDto create(final CampaignRequestDto request) {
    final Product product = productProcessor.saveProduct(request);
    final Campaign campaign = campaignService.create(campaignMapper.toCampaignEntity(request));
    return campaignMapper.toResponse(campaign);
  }
}
```

Rules:
- Use `org.springframework.transaction.annotation.Transactional` (not `jakarta.transaction.Transactional`).
- Processors may return DTOs — they are the boundary between HTTP and domain layers when multi-step assembly is needed.
- Keep processors thin — orchestrate, don't duplicate business logic that belongs in services.

## Domain model objects

When a service needs to return aggregated data composed of multiple entities, define a plain Java class in `<module>/model/`. These are not JPA entities — they are transient view objects.

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Assignment {
  private Campaign campaign;
  private CampaignAssignment campaignAssignment;
  private CampaignSlot campaignSlot;
}
```

- Annotate with Lombok only (no `@Entity`, no `@Table`).
- The corresponding mapper method takes the model object as input.
- The service interface returns the model object, not the constituent entities.

## State machines

State machines are `@Component` beans with a static `Map<Status, Set<Status>>` transition table. Call `stateMachine.transition(entity, targetStatus)` — it mutates the entity's status in place or throws `InvalidStateTransitionException`.

```java
@Component
public class CampaignStateMachine {

  private static final Map<CampaignStatus, Set<CampaignStatus>> TRANSITIONS =
      Map.of(
          CAMPAIGN_STATUS_DRAFT, Set.of(CAMPAIGN_STATUS_ACTIVE),
          CAMPAIGN_STATUS_ACTIVE, Set.of(CAMPAIGN_STATUS_PAUSED, CAMPAIGN_STATUS_CLOSED));

  public void transition(final Campaign campaign, final CampaignStatus to) {
    final CampaignStatus from = campaign.getStatus();
    if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
      throw new InvalidStateTransitionException(
          "Cannot transition campaign from " + from + " to " + to);
    }
    campaign.setStatus(to);
  }
}
```

Capture any state-dependent flags **before** calling `transition()` — after the call the entity's status has already changed:

```java
// capture BEFORE transition
final boolean isPublish = campaign.getStatus() != CAMPAIGN_STATUS_ACTIVE
    && target == CAMPAIGN_STATUS_ACTIVE;
stateMachine.transition(campaign, target);
if (isPublish) { ... }
```

## Exception hierarchy

Throw these from services and domain components — never `ApiException` (that is HTTP-layer only):

| Exception | HTTP status |
|---|---|
| `NotFoundException` | 404 |
| `ForbiddenException` | 403 |
| `InvalidStateTransitionException` | 409 |
| `BusinessRuleViolationException` | 422 |

`GlobalExceptionHandler` translates all of the above to JSON error responses with `error` and `message` fields.

## Logging

Use manual SLF4J — do not use Lombok `@Slf4j`:

```java
private static final Logger LOGGER = LoggerFactory.getLogger(ClaimServiceImpl.class);
```

The field must be named `LOGGER` (all caps) to satisfy the Checkstyle `ConstantName` rule.

Log levels:
- `LOGGER.debug(...)` — method entry, successful path milestones.
- `LOGGER.warn(...)` — business rule failures. Always include the key identifier (id, code) and the reason.
- `LOGGER.error(...)` — unexpected exceptions (in `GlobalExceptionHandler` only).

```java
if (claim.getStatus() != ClaimWorkflowStatus.ORDERED) {
  LOGGER.warn("Claim {} in status {} cannot transition to PROOF_SUBMITTED",
      claimId, claim.getStatus());
  throw new BusinessRuleViolationException("...");
}
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

## Database migrations

Flyway migrations live in `src/main/resources/db/migration/`. Naming convention:

```
V{4-digit-zero-padded-sequence}__{snake_case_description}.sql
```

Examples: `V0001__baseline.sql`, `V0002__add_claims_table.sql`.

Never modify an existing migration that has been applied to any environment — always add a new file.

## Utilities

### Shared utilities (`shared/util/`)

- `CodeGenerator.generateHumanCode("PREFIX")` — generates a human-readable unique code (e.g. `INV-ABCD1234`). Use for invite codes, coupon codes, and any user-facing identifiers.
- `DateTimeUtils.toLocalDate(int date)` — parses an integer in `YYYYMMDD` format to `LocalDate` using `DateTimeFormatter.BASIC_ISO_DATE`. Use for date fields stored as `int`.
- `PasswordService.hashPassword(String)` / `verifyPassword(String, String)` — BCrypt hash and verify. Hashing always happens in the service layer via `toBuilder()`, never in a mapper.

### Module utilities (`<module>/util/`)

Modules may have their own utility classes under `<module>/util/`. Follow the same pattern as `shared/util/`: `final` class, `private` constructor, static methods only:

```java
public final class SettingsUtils {

  private SettingsUtils() {}

  public static Settings getAdminSettings() {
    return Settings.builder()
        .isDashboardTabEnabled(true)
        .build();
  }
}
```

## Storage

`StorageService` (in `storage/service/`) abstracts file storage. Inject it wherever screenshot or file upload handling is needed:

```java
String key = storageService.store("claims", originalFilename, contentType, bytes);
byte[] data = storageService.retrieve(key);
storageService.delete(key);
```

The `store` method returns a storage key that should be persisted. The active implementation (local vs. Garage/S3) is selected via Spring profile configuration.

## Security

- `@CurrentUserId` injects the authenticated user's `UUID` into controller parameters.
- Use it to pass the caller identity into service methods that need ownership checks.
- Ownership checks belong in the service layer — controllers pass `requesterId` through, services verify it.

## Unit testing

### Stack

- JUnit 5 (`@ExtendWith(MockitoExtension.class)`) + Mockito for service unit tests.
- Assertions: JUnit 5 only (`assertEquals`, `assertTrue`, `assertFalse`, `assertThrows`). Do not use AssertJ.
- No `any()` or other generic/lenient Mockito matchers — always pass exact arguments.
- Use `doReturn(...).when(mock).method(args)` for stubbing.

### Fixtures

Fixture files are split into two directories under `src/test/resources/fixtures/`:

```
fixtures/
  input/<module>/    raw inputs passed into the service
  output/<module>/   expected results returned by the service
```

Load them with `FileUtils.loadResourceAsObject(path, Type.class)` — resource paths must start with `/`.

Each module has a `Fixtures` class (`final`, private constructor, package-private constants) co-located with the test in `src/test/java/.../service/impl/`:

```java
final class Fixtures {

  static final UUID CLAIM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  static final UUID OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  static final Claim CLAIM_1 =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/claim-1.json", Claim.class);

  static final Claim EXPECTED_CLAIM_1 =
      FileUtils.loadResourceAsObject("/fixtures/output/claim/claim-1.json", Claim.class);

  private Fixtures() {}
}
```

Rules:
- Input fixtures (`CLAIM_*`) and output fixtures (`EXPECTED_*`) are both loaded from JSON — never compute expected values in code via `toBuilder()`.
- The UUID constant (e.g. `CLAIM_ID`) must match the `id` field in the input fixture.
- Shared identifiers and primitive values live in `Fixtures`, not in the test class.

### JSON fixture format

Enums are serialized as their string name. Timestamps use ISO-8601. Omit fields that are `null` or not relevant to the test:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "ownerId": "22222222-2222-2222-2222-222222222222",
  "status": "ORDERED",
  "isDeleted": false,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Test class structure

```java
@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

  @Mock private ClaimRepository mockClaimRepository;
  @Mock private DealService mockDealService;
  private ClaimServiceImpl claimService;

  @BeforeEach
  void setUp() {
    this.claimService = new ClaimServiceImpl(
        this.mockClaimRepository, this.mockDealService);
  }

  @Test
  void testGetByIdWhenFound() {
    doReturn(Optional.of(CLAIM_1)).when(this.mockClaimRepository)
        .findByIdAndIsDeletedFalse(CLAIM_ID);

    final Claim result = this.claimService.getById(CLAIM_ID, OWNER_ID);

    assertEquals(CLAIM_1, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockClaimRepository)
        .findByIdAndIsDeletedFalse(CLAIM_ID);

    assertThrows(NotFoundException.class, () -> this.claimService.getById(CLAIM_ID, OWNER_ID));
  }
}
```

- Instantiate the service under test manually in `@BeforeEach setUp()` — do not use `@InjectMocks`.
- Mock fields use the `mock` prefix: `mockClaimRepository`, `mockDealService`.
- Wildcard static imports are allowed in test files for `Fixtures.*` and `Assertions.*`.
- Test method names: `test` + `MethodName` + `When` + `Condition`. Omit `When...` when there is only one path.
- For methods that build objects internally (e.g. soft delete via `toBuilder()`), use `ArgumentCaptor` and assert individual fields rather than constructing an expected object.

## Code style

- Spotless with Google Java Format is enforced — run `./gradlew spotlessApply` before committing.
- Checkstyle uses `google_checks.xml` (test sources excluded). `maxWarnings = 0`.
- Annotation processor order in `build.gradle` must be: lombok → lombok-mapstruct-binding → mapstruct-processor.
- All method parameters and local variables that are never reassigned use `final`.
- No wildcard imports (`import foo.*`) in main sources — always use explicit imports, including `static` imports and `org.mapstruct.*`, `jakarta.persistence.*`, `lombok.*`. Exception: test files may use wildcard static imports for `Fixtures.*` and `Assertions.*`.
- Always use braces on `if`/`else`/`for`/`while` — even single-line bodies.
- No comments unless the WHY is non-obvious.
