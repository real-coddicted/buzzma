package com.coddicted.buzzma.configsdk.health;

import com.coddicted.buzzma.configsdk.ConfigClient;
import com.coddicted.buzzma.configsdk.model.NamespaceDiagnostics;
import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Surfaces per-namespace cache age and poll health in Actuator's {@code /health}.
 *
 * <p>Deliberately always reports {@code UP}: this service is never on the application hot path
 * (design doc — "never a source of cascading failure"), so a config staleness problem must not flip
 * the consuming app's health check to DOWN and trigger an orchestrator restart. Staleness is
 * exposed as a detail for humans/dashboards to act on, not as a liveness signal.
 */
public class ConfigSdkHealthIndicator implements HealthIndicator {

  private final ConfigClient configClient;

  public ConfigSdkHealthIndicator(final ConfigClient configClient) {
    this.configClient = configClient;
  }

  @Override
  public Health health() {
    final Health.Builder builder = Health.up();
    final Map<String, NamespaceDiagnostics> diagnostics = configClient.diagnostics();
    for (final Map.Entry<String, NamespaceDiagnostics> entry : diagnostics.entrySet()) {
      final NamespaceDiagnostics d = entry.getValue();
      builder.withDetail(
          entry.getKey(),
          Map.of(
              "entryCount", d.entryCount(),
              "lastSuccessfulPollAt", String.valueOf(d.lastSuccessfulPollAt()),
              "consecutiveFailures", d.consecutiveFailures(),
              "pollIntervalSeconds", d.pollIntervalSeconds()));
    }
    return builder.build();
  }
}
