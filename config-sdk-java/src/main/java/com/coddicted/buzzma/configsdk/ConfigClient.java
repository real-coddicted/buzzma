package com.coddicted.buzzma.configsdk;

import com.coddicted.buzzma.configsdk.client.BulkFetchResult;
import com.coddicted.buzzma.configsdk.client.ConfigApiClient;
import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.coddicted.buzzma.configsdk.model.NamespaceDiagnostics;
import com.coddicted.buzzma.configsdk.snapshot.DiskSnapshotStore;
import com.coddicted.buzzma.configsdk.snapshot.SnapshotFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClient;

/**
 * Main entry point. A single client holds one independent cache, version pointer, and poll cadence
 * per registered namespace (design doc §7).
 *
 * <pre>{@code
 * ConfigClient client = ConfigClient.builder()
 *     .apiUrl("https://config-api.internal")
 *     .environment("prod")
 *     .register("checkout-service")
 *     .register("payments-service")
 *     .build();
 *
 * NamespaceConfig checkout = client.forNamespace("checkout-service");
 * checkout.getBool("new_checkout_flow", false);
 * }</pre>
 *
 * <p>In a Spring Boot application, prefer autowiring the bean {@code ConfigSdkAutoConfiguration}
 * creates from {@code config-sdk.*} properties over calling {@link #builder()} directly — the
 * autoconfigured bean shares the app's lifecycle and a properly-timed-out {@link RestClient}. The
 * public constructor and builder exist for manual/non-Spring use and tests.
 */
public class ConfigClient implements SmartLifecycle {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigClient.class);

  private final ConfigApiClient apiClient;
  private final DiskSnapshotStore snapshotStore;
  private final TaskScheduler taskScheduler;
  private final ObjectMapper objectMapper;
  private final Duration bootstrapTimeout;
  private final Duration maxBackoff;

  private final Map<String, NamespaceState> states = new ConcurrentHashMap<>();
  private final Map<String, NamespaceConfig> namespaceConfigs = new ConcurrentHashMap<>();
  private final Map<String, ConfigPoller> pollers = new ConcurrentHashMap<>();
  private volatile boolean running;

  public ConfigClient(
      final String environment,
      final List<String> namespaces,
      final int defaultPollIntervalSeconds,
      final Duration bootstrapTimeout,
      final Duration maxBackoff,
      final ConfigApiClient apiClient,
      final DiskSnapshotStore snapshotStore,
      final TaskScheduler taskScheduler,
      final ObjectMapper objectMapper) {
    this.apiClient = apiClient;
    this.snapshotStore = snapshotStore;
    this.taskScheduler = taskScheduler;
    this.objectMapper = objectMapper;
    this.bootstrapTimeout = bootstrapTimeout;
    this.maxBackoff = maxBackoff;

    for (final String namespace : namespaces) {
      final NamespaceState state =
          new NamespaceState(namespace, environment, defaultPollIntervalSeconds);
      states.put(namespace, state);
      namespaceConfigs.put(namespace, new NamespaceConfig(state, objectMapper));
    }
  }

  public static ConfigClientBuilder builder() {
    return new ConfigClientBuilder();
  }

  /** Returns the read API for a registered namespace. Throws if the namespace wasn't registered. */
  public NamespaceConfig forNamespace(final String namespace) {
    final NamespaceConfig config = namespaceConfigs.get(namespace);
    if (config == null) {
      throw new IllegalArgumentException(
          "Namespace '"
              + namespace
              + "' was not registered. Registered namespaces: "
              + namespaceConfigs.keySet());
    }
    return config;
  }

  /** Read-only per-namespace runtime state, consumed by the health indicator and metrics binder. */
  public Map<String, NamespaceDiagnostics> diagnostics() {
    return states.values().stream()
        .collect(Collectors.toUnmodifiableMap(NamespaceState::namespace, this::toDiagnostics));
  }

  private NamespaceDiagnostics toDiagnostics(final NamespaceState state) {
    return new NamespaceDiagnostics(
        state.namespace(),
        state.environment(),
        state.cacheSnapshot().size(),
        state.lastSuccessfulPollAt(),
        state.consecutiveFailures(),
        state.totalPollSuccessCount(),
        state.totalPollFailureCount(),
        state.cacheHitCount(),
        state.cacheMissCount(),
        state.pollIntervalSeconds());
  }

  // ── SmartLifecycle ─────────────────────────────────────────────────────────
  // Runs as part of Spring's startup phase, before the app reports ready — but bounded by
  // bootstrapTimeout per namespace so a Config API outage can never prevent an unrelated
  // service from starting (design doc §7 "Startup sequence" / "Hard requirement").

  @Override
  public void start() {
    final ExecutorService bootstrapExecutor = Executors.newVirtualThreadPerTaskExecutor();
    try {
      final List<CompletableFuture<Void>> futures =
          states.values().stream()
              .map(state -> bootstrapNamespace(state, bootstrapExecutor))
              .toList();
      CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    } finally {
      // shutdownNow(), not close()/shutdown() — close() blocks awaiting termination of any task
      // still running past its own orTimeout(), which would defeat the point of the timeout.
      // start() must return once every future is settled, not once every task has finished.
      bootstrapExecutor.shutdownNow();
    }

    for (final NamespaceState state : states.values()) {
      final ConfigPoller poller =
          new ConfigPoller(state, apiClient, snapshotStore, taskScheduler, maxBackoff);
      pollers.put(state.namespace(), poller);
      poller.start();
    }
    running = true;
  }

  private CompletableFuture<Void> bootstrapNamespace(
      final NamespaceState state, final ExecutorService executor) {
    return CompletableFuture.supplyAsync(
            () -> apiClient.bulkFetch(state.namespace(), state.environment()), executor)
        .orTimeout(bootstrapTimeout.toMillis(), TimeUnit.MILLISECONDS)
        .handle((result, error) -> applyBootstrapResult(state, result, error));
  }

  private Void applyBootstrapResult(
      final NamespaceState state, final BulkFetchResult result, final Throwable error) {
    if (error == null) {
      final Map<String, ConfigEntry> entries = toKeyedMap(result.getItems(), ConfigEntry::getKey);
      state.replaceCache(entries, result.getSnapshotChangeSeq());
      state.recordSuccessfulPoll();
      snapshotStore.write(
          state.namespace(), state.environment(), result.getSnapshotChangeSeq(), entries.values());
      LOG.info("Bootstrapped namespace '{}' with {} entries", state.namespace(), entries.size());
      return null;
    }

    LOG.warn(
        "Bulk fetch failed for namespace '{}': {} — falling back to disk snapshot",
        state.namespace(),
        error.getMessage());
    final Optional<SnapshotFile> snapshot =
        snapshotStore.read(state.namespace(), state.environment());
    if (snapshot.isPresent()) {
      final Map<String, ConfigEntry> entries =
          toKeyedMap(snapshot.get().getItems(), ConfigEntry::getKey);
      state.replaceCache(entries, snapshot.get().getSnapshotChangeSeq());
      LOG.info(
          "Loaded {} entries for namespace '{}' from disk snapshot written at {}",
          entries.size(),
          state.namespace(),
          snapshot.get().getWrittenAt());
    } else {
      LOG.warn(
          "No disk snapshot for namespace '{}' — every get() call returns the caller-provided "
              + "default until the next successful poll",
          state.namespace());
    }
    return null;
  }

  private Map<String, ConfigEntry> toKeyedMap(
      final List<ConfigEntry> items, final Function<ConfigEntry, String> keyFn) {
    return items.stream()
        .collect(Collectors.toUnmodifiableMap(keyFn, Function.identity(), (a, b) -> b));
  }

  @Override
  public void stop() {
    pollers.values().forEach(ConfigPoller::stop);
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  /** Manual/non-Spring builder — see class Javadoc. Builds its own default RestClient/scheduler. */
  public static final class ConfigClientBuilder {

    private String apiUrl;
    private String environment;
    private final List<String> namespaces = new java.util.ArrayList<>();
    private Duration bootstrapTimeout = Duration.ofSeconds(3);
    private int defaultPollIntervalSeconds = 60;
    private Duration maxBackoff = Duration.ofMinutes(5);
    private Path snapshotDir = Paths.get(System.getProperty("java.io.tmpdir"), "buzzma-config-sdk");

    private ConfigClientBuilder() {}

    public ConfigClientBuilder apiUrl(final String apiUrl) {
      this.apiUrl = apiUrl;
      return this;
    }

    public ConfigClientBuilder environment(final String environment) {
      this.environment = environment;
      return this;
    }

    public ConfigClientBuilder register(final String namespace) {
      this.namespaces.add(namespace);
      return this;
    }

    public ConfigClientBuilder bootstrapTimeout(final Duration bootstrapTimeout) {
      this.bootstrapTimeout = bootstrapTimeout;
      return this;
    }

    public ConfigClientBuilder defaultPollIntervalSeconds(final int defaultPollIntervalSeconds) {
      this.defaultPollIntervalSeconds = defaultPollIntervalSeconds;
      return this;
    }

    public ConfigClientBuilder maxBackoff(final Duration maxBackoff) {
      this.maxBackoff = maxBackoff;
      return this;
    }

    public ConfigClientBuilder snapshotDir(final Path snapshotDir) {
      this.snapshotDir = snapshotDir;
      return this;
    }

    public ConfigClient build() {
      final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

      // Spring Boot 3.3-compatible timeout builder — 3.4+ introduced a newer
      // ClientHttpRequestFactoryBuilder API in a different package; this repo pins 3.3.4.
      final ClientHttpRequestFactorySettings settings =
          ClientHttpRequestFactorySettings.DEFAULTS
              .withConnectTimeout(Duration.ofSeconds(2))
              .withReadTimeout(bootstrapTimeout);

      final RestClient restClient =
          RestClient.builder()
              .baseUrl(apiUrl)
              .requestFactory(ClientHttpRequestFactories.get(settings))
              .messageConverters(
                  converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                  })
              .build();

      final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
      scheduler.setPoolSize(Math.max(1, namespaces.size()));
      scheduler.setThreadNamePrefix("config-sdk-poller-");
      scheduler.initialize();

      return new ConfigClient(
          environment,
          List.copyOf(namespaces),
          defaultPollIntervalSeconds,
          bootstrapTimeout,
          maxBackoff,
          new ConfigApiClient(restClient),
          new DiskSnapshotStore(snapshotDir, objectMapper),
          scheduler,
          objectMapper);
    }
  }
}
