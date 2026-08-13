package com.coddicted.buzzma.configsdk.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.configsdk.ConfigClient;
import com.coddicted.buzzma.configsdk.client.BulkFetchResult;
import com.coddicted.buzzma.configsdk.client.ConfigApiClient;
import com.coddicted.buzzma.configsdk.snapshot.DiskSnapshotStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class ConfigSdkMetricsTest {

  @TempDir Path tempDir;

  private ThreadPoolTaskScheduler scheduler;
  private ConfigClient client;

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
  void bindsPerNamespaceGaugesAndCounters() {
    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    when(apiClient.bulkFetch(anyString(), anyString()))
        .thenReturn(
            BulkFetchResult.builder()
                .namespace("checkout-service")
                .environment("prod")
                .snapshotChangeSeq(1)
                .items(List.of())
                .build());

    client = newClient(apiClient);
    client.start();

    final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    new ConfigSdkMetrics(client, registry);

    assertThat(
            registry
                .get("config.sdk.cache.entries")
                .tag("namespace", "checkout-service")
                .gauge()
                .value())
        .isEqualTo(0.0);
    assertThat(
            registry
                .get("config.sdk.poll.success")
                .tag("namespace", "checkout-service")
                .functionCounter()
                .count())
        .isEqualTo(1.0);
    assertThat(
            registry
                .get("config.sdk.cache.hit.ratio")
                .tag("namespace", "checkout-service")
                .gauge()
                .value())
        .isEqualTo(1.0);
  }

  private ConfigClient newClient(final ConfigApiClient apiClient) {
    final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("test-config-sdk-poller-");
    scheduler.initialize();
    return new ConfigClient(
        "prod",
        List.of("checkout-service"),
        60,
        Duration.ofSeconds(3),
        Duration.ofMinutes(5),
        apiClient,
        new DiskSnapshotStore(tempDir, objectMapper),
        scheduler,
        objectMapper);
  }
}
