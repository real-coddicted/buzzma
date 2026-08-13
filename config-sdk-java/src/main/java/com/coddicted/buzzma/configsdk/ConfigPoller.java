package com.coddicted.buzzma.configsdk;

import com.coddicted.buzzma.configsdk.client.ConfigApiClient;
import com.coddicted.buzzma.configsdk.client.DeltaPollResult;
import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.coddicted.buzzma.configsdk.model.EntryStatus;
import com.coddicted.buzzma.configsdk.snapshot.DiskSnapshotStore;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

/**
 * One independent, self-rescheduling poll task per namespace (design doc §7 "Poller"). A shared
 * {@link TaskScheduler} runs every namespace's task on its own server-driven interval — namespaces
 * can legitimately be told to poll at different rates.
 *
 * <p>A failed poll never clears or degrades the cache — it just means slightly stale data, an
 * explicitly accepted tradeoff given the 1-2 minute propagation tolerance (design doc §1).
 */
final class ConfigPoller {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigPoller.class);
  private static final double MIN_JITTER_FRACTION = 0.10;
  private static final double MAX_JITTER_FRACTION = 0.20;

  private final NamespaceState state;
  private final ConfigApiClient apiClient;
  private final DiskSnapshotStore snapshotStore;
  private final TaskScheduler taskScheduler;
  private final Duration maxBackoff;

  ConfigPoller(
      final NamespaceState state,
      final ConfigApiClient apiClient,
      final DiskSnapshotStore snapshotStore,
      final TaskScheduler taskScheduler,
      final Duration maxBackoff) {
    this.state = state;
    this.apiClient = apiClient;
    this.snapshotStore = snapshotStore;
    this.taskScheduler = taskScheduler;
    this.maxBackoff = maxBackoff;
  }

  void start() {
    scheduleNext(jitteredDelay(state.pollIntervalSeconds()));
  }

  void stop() {
    state.cancelScheduledPoll();
  }

  private void scheduleNext(final Duration delay) {
    if (state.isStopped()) {
      return;
    }
    final ScheduledFuture<?> future = taskScheduler.schedule(this::poll, Instant.now().plus(delay));
    state.trackScheduledPoll(future);
  }

  private void poll() {
    if (state.isStopped()) {
      return;
    }
    try {
      final DeltaPollResult result =
          apiClient.deltaPoll(state.namespace(), state.environment(), state.snapshotChangeSeq());
      applyDelta(result);
      state.resetFailures();
      state.recordSuccessfulPoll();
      state.updatePollIntervalSeconds(result.getPollIntervalSeconds());
      snapshotStore.write(
          state.namespace(),
          state.environment(),
          state.snapshotChangeSeq(),
          state.cacheSnapshot().values());
      scheduleNext(jitteredDelay(state.pollIntervalSeconds()));
    } catch (final Exception e) {
      final int failures = state.recordFailureAndGetCount();
      LOG.warn(
          "Config delta poll failed for {}/{} (consecutive failures={}): {}",
          state.namespace(),
          state.environment(),
          failures,
          e.getMessage());
      scheduleNext(backoffDelay(failures));
    }
  }

  private void applyDelta(final DeltaPollResult result) {
    if (result.getItems() == null || result.getItems().isEmpty()) {
      // Server returns snapshotChangeSeq == sinceChangeSeq when nothing changed — the common
      // case on every routine poll. Skip the map rebuild entirely rather than swap in a copy.
      return;
    }
    final Map<String, ConfigEntry> updated = new HashMap<>(state.cacheSnapshot());
    for (final ConfigEntry entry : result.getItems()) {
      if (entry.getStatus() == EntryStatus.DELETED) {
        updated.remove(entry.getKey());
      } else {
        updated.put(entry.getKey(), entry);
      }
    }
    state.replaceCache(Map.copyOf(updated), result.getSnapshotChangeSeq());
  }

  private Duration jitteredDelay(final int baseSeconds) {
    final double jitterFraction =
        ThreadLocalRandom.current().nextDouble(MIN_JITTER_FRACTION, MAX_JITTER_FRACTION)
            * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
    final long millis = Math.round(baseSeconds * 1000L * (1 + jitterFraction));
    return Duration.ofMillis(Math.max(millis, 1000));
  }

  private Duration backoffDelay(final int consecutiveFailures) {
    final double backoffSeconds =
        state.pollIntervalSeconds() * Math.pow(2, consecutiveFailures - 1);
    final long boundedSeconds = Math.min((long) backoffSeconds, maxBackoff.getSeconds());
    return jitteredDelay((int) boundedSeconds);
  }
}
