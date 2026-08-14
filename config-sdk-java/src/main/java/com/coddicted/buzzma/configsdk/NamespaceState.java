package com.coddicted.buzzma.configsdk;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Per-namespace mutable state, shared between the poller thread (writer) and application threads
 * calling {@link NamespaceConfig} getters (readers) concurrently.
 *
 * <p>The one place a bug here would actually hurt (design doc §7 "Concurrency"): the poller builds
 * a brand new immutable map from old-map + delta and swaps a single reference. Readers always see
 * either the fully-old or fully-new snapshot, never a half-updated one — no locks on the read path.
 */
final class NamespaceState {

  private final String namespace;
  private final String environment;
  private final AtomicReference<Map<String, ConfigEntry>> cache = new AtomicReference<>(Map.of());
  private final AtomicLong snapshotChangeSeq = new AtomicLong(0);
  private final AtomicInteger pollIntervalSeconds;
  private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
  private final AtomicReference<ScheduledFuture<?>> scheduledPoll = new AtomicReference<>();
  private final LongAdder cacheHits = new LongAdder();
  private final LongAdder cacheMisses = new LongAdder();
  private final LongAdder totalPollSuccesses = new LongAdder();
  private final LongAdder totalPollFailures = new LongAdder();
  private volatile Instant lastSuccessfulPollAt;
  private volatile boolean stopped;

  NamespaceState(
      final String namespace, final String environment, final int defaultPollIntervalSeconds) {
    this.namespace = namespace;
    this.environment = environment;
    this.pollIntervalSeconds = new AtomicInteger(defaultPollIntervalSeconds);
  }

  String namespace() {
    return namespace;
  }

  String environment() {
    return environment;
  }

  Map<String, ConfigEntry> cacheSnapshot() {
    return cache.get();
  }

  void replaceCache(final Map<String, ConfigEntry> newCache, final long newSnapshotChangeSeq) {
    cache.set(newCache);
    snapshotChangeSeq.set(newSnapshotChangeSeq);
  }

  long snapshotChangeSeq() {
    return snapshotChangeSeq.get();
  }

  int pollIntervalSeconds() {
    return pollIntervalSeconds.get();
  }

  void updatePollIntervalSeconds(final int seconds) {
    pollIntervalSeconds.set(seconds);
  }

  int recordFailureAndGetCount() {
    totalPollFailures.increment();
    return consecutiveFailures.incrementAndGet();
  }

  void resetFailures() {
    consecutiveFailures.set(0);
  }

  int consecutiveFailures() {
    return consecutiveFailures.get();
  }

  void recordSuccessfulPoll() {
    totalPollSuccesses.increment();
    lastSuccessfulPollAt = Instant.now();
  }

  Instant lastSuccessfulPollAt() {
    return lastSuccessfulPollAt;
  }

  void trackScheduledPoll(final ScheduledFuture<?> future) {
    scheduledPoll.set(future);
  }

  void cancelScheduledPoll() {
    stopped = true;
    final ScheduledFuture<?> future = scheduledPoll.get();
    if (future != null) {
      future.cancel(false);
    }
  }

  boolean isStopped() {
    return stopped;
  }

  void recordCacheHit() {
    cacheHits.increment();
  }

  void recordCacheMiss() {
    cacheMisses.increment();
  }

  long cacheHitCount() {
    return cacheHits.sum();
  }

  long cacheMissCount() {
    return cacheMisses.sum();
  }

  long totalPollSuccessCount() {
    return totalPollSuccesses.sum();
  }

  long totalPollFailureCount() {
    return totalPollFailures.sum();
  }
}
