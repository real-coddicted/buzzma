package com.coddicted.buzzma.configsdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.configsdk.client.BulkFetchResult;
import com.coddicted.buzzma.configsdk.client.ConfigApiClient;
import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.coddicted.buzzma.configsdk.model.ValueType;
import com.coddicted.buzzma.configsdk.snapshot.DiskSnapshotStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Covers the bootstrap fallback chain from design doc §7: in-memory -> disk snapshot -> caller
 * default.
 */
class ConfigClientTest {

  private static final String NAMESPACE = "checkout-service";
  private static final String ENVIRONMENT = "prod";

  @TempDir Path tempDir;

  private ConfigClient client;
  private ThreadPoolTaskScheduler scheduler;

  @AfterEach
  void tearDown() {
    if (client != null) {
      client.stop();
    }
    if (scheduler != null) {
      scheduler.shutdown();
    }
  }

  @Test
  void bootstrapPopulatesCacheOnSuccessfulBulkFetch() {
    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    final ConfigEntry entry = entry("flag");
    when(apiClient.bulkFetch(NAMESPACE, ENVIRONMENT))
        .thenReturn(
            BulkFetchResult.builder()
                .namespace(NAMESPACE)
                .environment(ENVIRONMENT)
                .snapshotChangeSeq(1)
                .items(List.of(entry))
                .build());

    client = newClient(apiClient, Duration.ofSeconds(3));
    client.start();

    assertThat(client.forNamespace(NAMESPACE).getBool("flag", false)).isTrue();
    assertThat(client.diagnostics().get(NAMESPACE).entryCount()).isEqualTo(1);
  }

  @Test
  void bootstrapFallsBackToDiskSnapshotWhenBulkFetchFails() {
    final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    final DiskSnapshotStore snapshotStore = new DiskSnapshotStore(tempDir, objectMapper);
    snapshotStore.write(NAMESPACE, ENVIRONMENT, 4, List.of(entry("flag_from_disk")));

    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    when(apiClient.bulkFetch(anyString(), anyString()))
        .thenThrow(new RuntimeException("config API down"));

    client = newClient(apiClient, snapshotStore, objectMapper, Duration.ofSeconds(3));
    client.start();

    assertThat(client.forNamespace(NAMESPACE).getBool("flag_from_disk", false)).isTrue();
  }

  @Test
  void bootstrapLeavesCacheEmptyWhenBulkFetchFailsAndNoSnapshotExists() {
    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    when(apiClient.bulkFetch(anyString(), anyString()))
        .thenThrow(new RuntimeException("config API down"));

    client = newClient(apiClient, Duration.ofSeconds(3));
    client.start();

    assertThat(client.forNamespace(NAMESPACE).getBool("anything", true)).isTrue();
    assertThat(client.diagnostics().get(NAMESPACE).entryCount()).isZero();
  }

  @Test
  void startNeverBlocksPastConfiguredBootstrapTimeout() {
    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    when(apiClient.bulkFetch(anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              Thread.sleep(2000);
              return BulkFetchResult.builder()
                  .namespace(NAMESPACE)
                  .environment(ENVIRONMENT)
                  .snapshotChangeSeq(0)
                  .items(List.of())
                  .build();
            });

    client = newClient(apiClient, Duration.ofMillis(300));

    final long startNanos = System.nanoTime();
    client.start();
    final long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

    assertThat(elapsedMs).isLessThan(1500);
    assertThat(client.diagnostics().get(NAMESPACE).entryCount()).isZero();
  }

  @Test
  void forNamespaceThrowsForUnregisteredNamespace() {
    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    when(apiClient.bulkFetch(anyString(), anyString()))
        .thenReturn(
            BulkFetchResult.builder()
                .namespace(NAMESPACE)
                .environment(ENVIRONMENT)
                .snapshotChangeSeq(0)
                .items(List.of())
                .build());
    client = newClient(apiClient, Duration.ofSeconds(3));
    client.start();

    assertThatThrownBy(() -> client.forNamespace("unknown-namespace"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unknown-namespace");
  }

  private ConfigEntry entry(final String key) {
    return ConfigEntry.builder()
        .key(key)
        .valueType(ValueType.BOOLEAN)
        .value(BooleanNode.TRUE)
        .changeSeq(1)
        .build();
  }

  private ConfigClient newClient(final ConfigApiClient apiClient, final Duration bootstrapTimeout) {
    final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    return newClient(
        apiClient, new DiskSnapshotStore(tempDir, objectMapper), objectMapper, bootstrapTimeout);
  }

  private ConfigClient newClient(
      final ConfigApiClient apiClient,
      final DiskSnapshotStore snapshotStore,
      final ObjectMapper objectMapper,
      final Duration bootstrapTimeout) {
    scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("test-config-sdk-poller-");
    scheduler.initialize();
    return new ConfigClient(
        ENVIRONMENT,
        List.of(NAMESPACE),
        60,
        bootstrapTimeout,
        Duration.ofMinutes(5),
        apiClient,
        snapshotStore,
        scheduler,
        objectMapper);
  }
}
