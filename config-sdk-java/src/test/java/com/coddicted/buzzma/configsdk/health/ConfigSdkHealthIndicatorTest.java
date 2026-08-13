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
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class ConfigSdkHealthIndicatorTest {

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
  void reportsUpWithPerNamespaceDetailsEvenWhenBulkFetchFailed() {
    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    when(apiClient.bulkFetch(anyString(), anyString())).thenThrow(new RuntimeException("down"));

    client = newClient(apiClient);
    client.start();

    final Health health = new ConfigSdkHealthIndicator(client).health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
    assertThat(health.getDetails()).containsKey("checkout-service");
  }

  @SuppressWarnings("unchecked")
  @Test
  void detailIncludesEntryCountAfterSuccessfulBootstrap() {
    final ConfigApiClient apiClient = mock(ConfigApiClient.class);
    when(apiClient.bulkFetch("checkout-service", "prod"))
        .thenReturn(
            BulkFetchResult.builder()
                .namespace("checkout-service")
                .environment("prod")
                .snapshotChangeSeq(1)
                .items(List.of())
                .build());

    client = newClient(apiClient);
    client.start();

    final Health health = new ConfigSdkHealthIndicator(client).health();
    final var detail = (java.util.Map<String, Object>) health.getDetails().get("checkout-service");
    assertThat(detail).containsEntry("entryCount", 0);
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
