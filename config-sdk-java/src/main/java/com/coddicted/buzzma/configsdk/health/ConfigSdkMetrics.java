package com.coddicted.buzzma.configsdk.health;

import com.coddicted.buzzma.configsdk.ConfigClient;
import com.coddicted.buzzma.configsdk.model.NamespaceDiagnostics;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;

/**
 * Binds per-namespace gauges/counters to Micrometer (design doc §7: "cache hit ratio, poll
 * failures, fallback-chain depth reached"). Each meter reads live from {@link
 * ConfigClient#diagnostics()} on every scrape rather than being event-driven, so there is nothing
 * here to keep in sync with the poller — one less place a bug could cause metrics to drift from
 * reality.
 */
public class ConfigSdkMetrics {

  public ConfigSdkMetrics(final ConfigClient configClient, final MeterRegistry registry) {
    for (final String namespace : configClient.diagnostics().keySet()) {
      Gauge.builder(
              "config.sdk.cache.entries", configClient, c -> diagnostics(c, namespace).entryCount())
          .tag("namespace", namespace)
          .description("Number of active config entries currently cached for this namespace")
          .register(registry);

      Gauge.builder(
              "config.sdk.cache.age.seconds", configClient, c -> cacheAgeSeconds(c, namespace))
          .tag("namespace", namespace)
          .description("Seconds since the last successful bootstrap or poll; -1 if never succeeded")
          .register(registry);

      Gauge.builder("config.sdk.cache.hit.ratio", configClient, c -> cacheHitRatio(c, namespace))
          .tag("namespace", namespace)
          .description(
              "Fraction of get() calls resolved from the cache rather than a caller default")
          .register(registry);

      Gauge.builder(
              "config.sdk.poll.consecutive.failures",
              configClient,
              c -> diagnostics(c, namespace).consecutiveFailures())
          .tag("namespace", namespace)
          .description("Consecutive failed delta polls since the last success")
          .register(registry);

      FunctionCounter.builder(
              "config.sdk.poll.success",
              configClient,
              c -> diagnostics(c, namespace).totalPollSuccesses())
          .tag("namespace", namespace)
          .register(registry);

      FunctionCounter.builder(
              "config.sdk.poll.failure",
              configClient,
              c -> diagnostics(c, namespace).totalPollFailures())
          .tag("namespace", namespace)
          .register(registry);
    }
  }

  private static NamespaceDiagnostics diagnostics(
      final ConfigClient client, final String namespace) {
    return client.diagnostics().get(namespace);
  }

  private static double cacheAgeSeconds(final ConfigClient client, final String namespace) {
    final Instant lastSuccess = diagnostics(client, namespace).lastSuccessfulPollAt();
    return lastSuccess == null ? -1 : Duration.between(lastSuccess, Instant.now()).getSeconds();
  }

  private static double cacheHitRatio(final ConfigClient client, final String namespace) {
    final NamespaceDiagnostics d = diagnostics(client, namespace);
    final long total = d.cacheHits() + d.cacheMisses();
    return total == 0 ? 1.0 : (double) d.cacheHits() / total;
  }
}
