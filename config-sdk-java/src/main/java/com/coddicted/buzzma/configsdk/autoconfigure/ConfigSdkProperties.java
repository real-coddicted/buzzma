package com.coddicted.buzzma.configsdk.autoconfigure;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code config-sdk.*} properties, per the Spring Boot Starter shape in the design doc §7.
 *
 * <pre>{@code
 * config-sdk:
 *   api-url: https://config-api.internal
 *   environment: prod
 *   namespaces:
 *     - checkout-service
 *     - payments-service
 *   bootstrap-timeout: 3s
 * }</pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "config-sdk")
public class ConfigSdkProperties {

  /** Base URL of the Config API service. */
  private String apiUrl;

  /** Environment this instance serves — recorded metadata, not a query filter (design doc §4). */
  private String environment;

  /** Namespaces this client bulk-fetches and polls on its own independent cadence. */
  private List<String> namespaces = List.of();

  /** Bootstrap bulk-fetch timeout — startup must never block indefinitely (design doc §7). */
  private Duration bootstrapTimeout = Duration.ofSeconds(3);

  /** Used only before any delta poll has ever succeeded for a namespace (design doc §7). */
  private int defaultPollIntervalSeconds = 60;

  /** Ceiling for exponential backoff after consecutive poll failures. */
  private Duration maxBackoff = Duration.ofMinutes(5);

  /** Directory for the per-namespace disk snapshot used to survive a restart (design doc §7). */
  private String snapshotDir = System.getProperty("java.io.tmpdir") + "/buzzma-config-sdk";
}
