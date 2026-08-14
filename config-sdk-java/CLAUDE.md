# Config SDK (Java) — CLAUDE.md

**Full design rationale — including why this is a sibling module rather than a
subproject of `configurator/` — lives in
[`../configurator/docs/architecture/config-service-design.md`](../configurator/docs/architecture/config-service-design.md)
§7 (SDK design) and §10 (implementation order / repo layout decision). Treat
every decision documented there as settled unless you surface a conflict
explicitly.**

---

## What this module is

A Spring Boot Starter that consuming services embed to read runtime
config/feature-flag values from the [`configurator`](../configurator/) service
without ever making a network call on the application's hot path. It bulk-fetches
on startup, then polls a delta endpoint on a background schedule, and serves
reads from a local in-memory cache via lock-free atomic swap.

This is a **sibling Gradle module** to `configurator/` — own `build.gradle`,
own `gradlew`, own version (`0.1.0-SNAPSHOT` currently), independently
buildable and publishable. It is not a subproject of `configurator/`.

---

## Implementation phase

Phase 3 of the config service's overall rollout (see `configurator/CLAUDE.md`
for the full phase table). Implemented:

- Bootstrap: parallel per-namespace bulk fetch, bounded by `bootstrap-timeout`,
  never blocks app startup indefinitely (see "Bootstrap timeout" below).
- Fallback chain: in-memory cache → disk snapshot → caller-provided default.
- Poller: independent per-namespace scheduled task, server-driven interval +
  10–20% jitter, exponential backoff on failure (capped, jittered), never
  clears the cache on a failed poll.
- Atomic swap: `AtomicReference<Map<String, ConfigEntry>>` — readers never see
  a half-updated map, no locks on the read path.
- Disk snapshot: one JSON file per namespace+environment, `formatVersion`
  field, atomic rename on write.
- Health indicator (Actuator, if present) and Micrometer metrics (if a
  `MeterRegistry` bean exists) — both optional, both no-ops if absent.
- Autoconfiguration is entirely inert unless `config-sdk.api-url` is set —
  having this starter on the classpath is never itself a source of failure.

- Publishing to Gitea's built-in Maven package registry — see "Publishing"
  below.

Not yet built (deferred, see design doc §9):
- Contract-test job (SDK vs. a real running Config API service) — deferred to
  Phase 5, when the OpenAPI/behavioral-contract docs get written.
- Pilot integration into a real consuming service — that's Phase 4.

---

## Key constraints (not derivable from code)

- **Spring Boot 3.3.4, not 3.4+.** This pins the same version as
  `configurator/`. The design doc's example `RestClient` timeout snippet
  (§7) uses `ClientHttpRequestFactoryBuilder.detect()`, which is a Boot 3.4+
  API in a different package. This module uses the 3.3-compatible
  `org.springframework.boot.web.client.ClientHttpRequestFactories` /
  `ClientHttpRequestFactorySettings` instead — same intent (dedicated
  connect/read timeouts), different API surface. Re-check this if the repo
  ever bumps to Boot 3.4+.

- **`NamespaceState` is intentionally package-private**, living in the root
  `com.coddicted.buzzma.configsdk` package alongside `ConfigClient`,
  `NamespaceConfig`, and `ConfigPoller` — all four must stay in that same
  package for this to compile, since Java package-private access does not
  extend to subpackages. Don't move `ConfigPoller` into a `poller/`
  subpackage without also making `NamespaceState` public (which would leak
  a mutable internal collaborator as part of the public API — avoid it).

- **`ConfigClient`'s full constructor is public**, even though most users
  should go through `ConfigSdkAutoConfiguration` or `ConfigClient.builder()`
  instead. This is unavoidable: the autoconfiguration class lives in the
  `autoconfigure` subpackage, so a package-private constructor wouldn't be
  reachable from there either. The public constructor is documented as
  advanced/DI-oriented, not the primary entry point.

- **Bootstrap timeout is enforced with `shutdownNow()`, not
  `close()`/`shutdown()`.** `ConfigClient.start()` runs each namespace's bulk
  fetch on a virtual thread via `Executors.newVirtualThreadPerTaskExecutor()`,
  bounded per-future by `.orTimeout(bootstrapTimeout)`. The executor is
  explicitly `shutdownNow()`'d in a `finally` block rather than closed via
  try-with-resources — `ExecutorService.close()` blocks awaiting termination
  of any still-running task, which would silently defeat the timeout the
  moment a bulk fetch hangs past it. This was caught by
  `ConfigClientTest#startNeverBlocksPastConfiguredBootstrapTimeout` during
  Phase 3 implementation, not by inspection — if you touch `start()`, keep
  that test passing.

- **Micrometer's `@ConditionalOnClass(MeterRegistry.class)` alone is not
  enough** to gate `ConfigSdkMetrics` — the class can be on the classpath
  (e.g. transitively) with no actual `MeterRegistry` bean in the context.
  `ConfigSdkAutoConfiguration.MetricsConfiguration` uses both
  `@ConditionalOnClass` and `@ConditionalOnBean(MeterRegistry.class)`. This
  was caught by `ConfigSdkAutoConfigurationTest` failing with
  `UnsatisfiedDependencyException` during Phase 3 implementation — the
  `HealthConfiguration` nested class doesn't need the same treatment since
  `ConfigSdkHealthIndicator` doesn't depend on an existing bean of its own
  interface type.

- **`config-sdk.namespaces` empty-items delta-poll optimization.** When a
  delta poll response has no items (the common case — nothing changed since
  last poll), `ConfigPoller` skips rebuilding the cache map entirely rather
  than swapping in an equivalent copy. `NamespaceConfigTest` /
  `ConfigPollerTest` assert cache map identity (`isSameAs`) is preserved
  across a no-op poll — don't "simplify" this into an unconditional rebuild.

---

## Publishing

`build.gradle`'s `publishing.repositories` has two targets:

- **`mavenLocal()`** — always available, no credentials needed. Run
  `./gradlew publishToMavenLocal` to install a build into `~/.m2/repository`
  for local iteration against a consuming module before a real release.
- **`giteaPackages`** — Gitea's built-in Maven package registry, same
  instance already used for this repo's Docker images
  (`gitea.local.coddicted.com`), at
  `/api/packages/<owner>/maven`. Requires `PACKAGE_REGISTRY_TOKEN` (a Gitea
  access token with package write scope) in the environment — named without
  a `GITEA_` prefix since that prefix is reserved for Gitea's own
  injected secrets/variables and can't be used for user-defined ones.
  Without it, only the `giteaPackages` target fails — `publishToMavenLocal`
  is unaffected. CI (`.gitea/workflows/ci-config-sdk.yml`) runs
  `./gradlew publishAllPublicationsToGiteaPackagesRepository` on tag pushes
  matching `v*.*.*` only — plain branch pushes just run `check`.

**Consuming from the Gitea registry** (once a version is actually tagged and
published) requires the consuming module's own `build.gradle` to add the
same repository (with read credentials) and declare the dependency:

```groovy
repositories {
  maven {
    url = uri("https://gitea.local.coddicted.com/api/packages/coddicted/maven")
    credentials(HttpHeaderCredentials) {
      name = 'Authorization'
      value = "token ${System.getenv('PACKAGE_REGISTRY_TOKEN')}"
    }
    authentication { header(HttpHeaderAuthentication) }
  }
}
dependencies {
  implementation 'com.coddicted.buzzma:buzzma-config-sdk:0.1.0'
}
```

This is Phase 4 work (pilot integration), not yet done to any real module.

**Dependency versions are not pinned in the published POM/module metadata.**
`spring-boot-starter`/`spring-boot-autoconfigure` etc. appear with no
`<version>` — resolution relies on the *consumer* also applying a compatible
`spring-boot-dependencies` BOM (e.g. via the `org.springframework.boot`
Gradle plugin, which every current Boot app in this repo already applies).
This is the standard way `io.spring.dependency-management`-published
libraries work (it mirrors how Spring's own starter POMs are published) —
not a bug, but it means a non-Spring-Boot consumer would need to explicitly
import the same BOM version to resolve this artifact's dependencies.
`GenerateModuleMetadata`'s `dependencies-without-versions` validation is
suppressed in `build.gradle` for exactly this reason.

---

## Package layout

```
com.coddicted.buzzma.configsdk/
  ConfigClient          Public entry point, SmartLifecycle, builder
  NamespaceConfig        Public per-namespace read API (getBool/getString/getInt/getDouble/getJson)
  NamespaceState          package-private: atomic cache + poll bookkeeping
  ConfigPoller            package-private: per-namespace scheduled poll task
  autoconfigure/          ConfigSdkProperties, ConfigSdkAutoConfiguration
  client/                 ConfigApiClient (Fetcher), BulkFetchResult, DeltaPollResult
  model/                  ConfigEntry, ValueType, EntryStatus, NamespaceDiagnostics
  snapshot/               DiskSnapshotStore, SnapshotFile
  health/                 ConfigSdkHealthIndicator, ConfigSdkMetrics (both optional)
```

---

## Consuming this module (once published)

```yaml
config-sdk:
  api-url: https://config-api.internal
  environment: prod
  namespaces:
    - checkout-service
  bootstrap-timeout: 3s
```

```java
@Autowired ConfigClient configClient;
// ...
NamespaceConfig checkout = configClient.forNamespace("checkout-service");
boolean enabled = checkout.getBool("new_checkout_flow", false);
```

For manual/non-Spring use or tests, `ConfigClient.builder()...build()` is
self-sufficient — it constructs its own `RestClient`, `ObjectMapper`, and
`ThreadPoolTaskScheduler` rather than requiring Spring DI.

---

## Deferred / open items

Do not build these speculatively. Flag them to the user when they become relevant:

- Contract-test job against a real running `configurator` instance (Phase 5)
- Internal Maven/Gradle repo publishing target (the `maven-publish` block
  exists in `build.gradle`; only the repository coordinates are missing)
- Python SDK, and the shared OpenAPI/behavioral-contract docs both SDKs would
  follow (design doc §8) — this module is Java-only
- `onChange`/reactive callbacks on `NamespaceConfig` — explicitly rejected in
  the design doc given the 1-2 minute propagation tolerance; revisit only if
  a concrete use case shows up
