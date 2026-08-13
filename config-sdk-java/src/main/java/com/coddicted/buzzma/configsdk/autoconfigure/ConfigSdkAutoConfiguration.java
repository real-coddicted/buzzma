package com.coddicted.buzzma.configsdk.autoconfigure;

import com.coddicted.buzzma.configsdk.ConfigClient;
import com.coddicted.buzzma.configsdk.client.ConfigApiClient;
import com.coddicted.buzzma.configsdk.health.ConfigSdkHealthIndicator;
import com.coddicted.buzzma.configsdk.health.ConfigSdkMetrics;
import com.coddicted.buzzma.configsdk.snapshot.DiskSnapshotStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.file.Paths;
import java.time.Duration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClient;

/**
 * Autoconfigures the SDK from {@code config-sdk.*} properties (design doc §7). Every bean uses a
 * dedicated {@link RestClient}/{@link ObjectMapper}/{@link TaskScheduler} rather than whatever the
 * host app already has configured, so the SDK is isolated from unrelated changes to the app's own
 * HTTP/Jackson/scheduling setup.
 *
 * <p>Inactive entirely — no beans registered, no startup cost — unless {@code config-sdk.api-url}
 * is set, so simply having this starter on the classpath is never itself a source of failure.
 */
@AutoConfiguration
@EnableConfigurationProperties(ConfigSdkProperties.class)
@ConditionalOnProperty(prefix = "config-sdk", name = "api-url")
public class ConfigSdkAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "configSdkObjectMapper")
  public ObjectMapper configSdkObjectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule());
  }

  @Bean
  @ConditionalOnMissingBean(name = "configRestClient")
  public RestClient configRestClient(
      final ConfigSdkProperties props, final ObjectMapper configSdkObjectMapper) {
    // Spring Boot 3.3-compatible timeout builder; 3.4+ moved this to a different package.
    final ClientHttpRequestFactorySettings settings =
        ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofSeconds(2))
            .withReadTimeout(props.getBootstrapTimeout());

    return RestClient.builder()
        .baseUrl(props.getApiUrl())
        .requestFactory(ClientHttpRequestFactories.get(settings))
        .messageConverters(
            converters -> {
              converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
              converters.add(new MappingJackson2HttpMessageConverter(configSdkObjectMapper));
            })
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigApiClient configApiClient(final RestClient configRestClient) {
    return new ConfigApiClient(configRestClient);
  }

  @Bean(name = "configSdkTaskScheduler")
  @ConditionalOnMissingBean(name = "configSdkTaskScheduler")
  public TaskScheduler configSdkTaskScheduler(final ConfigSdkProperties props) {
    final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(Math.max(1, props.getNamespaces().size()));
    scheduler.setThreadNamePrefix("config-sdk-poller-");
    scheduler.initialize();
    return scheduler;
  }

  @Bean
  @ConditionalOnMissingBean
  public DiskSnapshotStore diskSnapshotStore(
      final ConfigSdkProperties props, final ObjectMapper configSdkObjectMapper) {
    return new DiskSnapshotStore(Paths.get(props.getSnapshotDir()), configSdkObjectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigClient configClient(
      final ConfigSdkProperties props,
      final ConfigApiClient configApiClient,
      final DiskSnapshotStore diskSnapshotStore,
      final TaskScheduler configSdkTaskScheduler,
      final ObjectMapper configSdkObjectMapper) {
    return new ConfigClient(
        props.getEnvironment(),
        props.getNamespaces(),
        props.getDefaultPollIntervalSeconds(),
        props.getBootstrapTimeout(),
        props.getMaxBackoff(),
        configApiClient,
        diskSnapshotStore,
        configSdkTaskScheduler,
        configSdkObjectMapper);
  }

  /** SmartLifecycle drives start()/stop() automatically — Spring never needs a destroy method. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(HealthIndicator.class)
  static class HealthConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ConfigSdkHealthIndicator configSdkHealthIndicator(final ConfigClient configClient) {
      return new ConfigSdkHealthIndicator(configClient);
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(MeterRegistry.class)
  @ConditionalOnBean(MeterRegistry.class)
  static class MetricsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ConfigSdkMetrics configSdkMetrics(
        final ConfigClient configClient, final MeterRegistry meterRegistry) {
      return new ConfigSdkMetrics(configClient, meterRegistry);
    }
  }
}
