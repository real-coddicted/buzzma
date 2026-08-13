package com.coddicted.buzzma.configsdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.configsdk.client.ConfigApiClient;
import com.coddicted.buzzma.configsdk.client.DeltaPollResult;
import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.coddicted.buzzma.configsdk.model.EntryStatus;
import com.coddicted.buzzma.configsdk.model.ValueType;
import com.coddicted.buzzma.configsdk.snapshot.DiskSnapshotStore;
import com.fasterxml.jackson.databind.node.BooleanNode;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

/**
 * poll() / jitteredDelay() / backoffDelay() are private — invoked via reflection to test the
 * poller's core logic in isolation, without the timing flakiness of driving it through the real
 * scheduler.
 */
class ConfigPollerTest {

  private NamespaceState state;
  private ConfigApiClient apiClient;
  private DiskSnapshotStore snapshotStore;
  private ConfigPoller poller;

  @BeforeEach
  void setUp() {
    state = new NamespaceState("checkout-service", "prod", 60);
    apiClient = mock(ConfigApiClient.class);
    snapshotStore = mock(DiskSnapshotStore.class);
    final TaskScheduler taskScheduler = mock(TaskScheduler.class);
    poller =
        new ConfigPoller(state, apiClient, snapshotStore, taskScheduler, Duration.ofMinutes(5));
  }

  @Test
  void successfulPollUpsertsAndRemovesEntriesThenResetsFailures() throws Exception {
    state.recordFailureAndGetCount();
    state.replaceCache(Map.of("stale_key", entry("stale_key", EntryStatus.ACTIVE)), 5);

    final ConfigEntry upserted = entry("new_checkout_flow", EntryStatus.ACTIVE);
    final ConfigEntry deleted = entry("stale_key", EntryStatus.DELETED);
    final DeltaPollResult result =
        DeltaPollResult.builder()
            .namespace("checkout-service")
            .environment("prod")
            .snapshotChangeSeq(9)
            .pollIntervalSeconds(45)
            .items(List.of(upserted, deleted))
            .build();
    when(apiClient.deltaPoll("checkout-service", "prod", 5)).thenReturn(result);

    invokePoll();

    assertThat(state.cacheSnapshot()).containsOnlyKeys("new_checkout_flow");
    assertThat(state.snapshotChangeSeq()).isEqualTo(9);
    assertThat(state.pollIntervalSeconds()).isEqualTo(45);
    assertThat(state.consecutiveFailures()).isZero();
    assertThat(state.totalPollSuccessCount()).isEqualTo(1);
    verify(snapshotStore).write(eq("checkout-service"), eq("prod"), eq(9L), any());
  }

  @Test
  void emptyDeltaSkipsCacheRebuildButStillCountsAsSuccess() throws Exception {
    final Map<String, ConfigEntry> originalMap = Map.of("flag", entry("flag", EntryStatus.ACTIVE));
    state.replaceCache(originalMap, 3);

    final DeltaPollResult result =
        DeltaPollResult.builder()
            .namespace("checkout-service")
            .environment("prod")
            .snapshotChangeSeq(3)
            .pollIntervalSeconds(45)
            .items(List.of())
            .build();
    when(apiClient.deltaPoll("checkout-service", "prod", 3)).thenReturn(result);

    invokePoll();

    assertThat(state.cacheSnapshot()).isSameAs(originalMap);
    assertThat(state.totalPollSuccessCount()).isEqualTo(1);
  }

  @Test
  void failedPollLeavesCacheUntouchedAndIncrementsFailureCount() throws Exception {
    final Map<String, ConfigEntry> originalMap = Map.of("flag", entry("flag", EntryStatus.ACTIVE));
    state.replaceCache(originalMap, 3);
    when(apiClient.deltaPoll(anyString(), anyString(), anyLong()))
        .thenThrow(new RuntimeException("boom"));

    invokePoll();

    assertThat(state.cacheSnapshot()).isSameAs(originalMap);
    assertThat(state.consecutiveFailures()).isEqualTo(1);
    assertThat(state.totalPollFailureCount()).isEqualTo(1);
  }

  @Test
  void jitteredDelayStaysWithinConfiguredBounds() throws Exception {
    final Method method = ConfigPoller.class.getDeclaredMethod("jitteredDelay", int.class);
    method.setAccessible(true);
    for (int i = 0; i < 200; i++) {
      final Duration delay = (Duration) method.invoke(poller, 100);
      assertThat(delay.toMillis()).isBetween(80_000L, 120_000L);
    }
  }

  @Test
  void backoffDelayIsBoundedByMaxBackoffPlusJitter() throws Exception {
    final Method method = ConfigPoller.class.getDeclaredMethod("backoffDelay", int.class);
    method.setAccessible(true);
    final Duration delay = (Duration) method.invoke(poller, 20);
    assertThat(delay).isBetween(Duration.ofSeconds(200), Duration.ofSeconds(370));
  }

  private void invokePoll() throws Exception {
    final Method method = ConfigPoller.class.getDeclaredMethod("poll");
    method.setAccessible(true);
    method.invoke(poller);
  }

  private ConfigEntry entry(final String key, final EntryStatus status) {
    return ConfigEntry.builder()
        .key(key)
        .valueType(ValueType.BOOLEAN)
        .value(BooleanNode.TRUE)
        .status(status)
        .changeSeq(1)
        .build();
  }
}
