package com.coddicted.buzzma.configsdk.model;

import java.time.Instant;

/** Read-only snapshot of one namespace's runtime state, for health checks and metrics. */
public record NamespaceDiagnostics(
    String namespace,
    String environment,
    int entryCount,
    Instant lastSuccessfulPollAt,
    int consecutiveFailures,
    long totalPollSuccesses,
    long totalPollFailures,
    long cacheHits,
    long cacheMisses,
    int pollIntervalSeconds) {}
