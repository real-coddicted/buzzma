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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Autoconfigures the SDK from {@code config-sdk.*} properties (design doc §7). Every bean uses a
 * dedicated {@link RestClient}/{@link ObjectMapper}/{@link TaskScheduler} rather than whatever the
 * host app already has configured, so the SDK is isolated from unrelated changes to the app's own
 * HTTP/Jackson/scheduling setup. In particular, neither the {@link ObjectMapper} nor the {@link
 * TaskScheduler} built here is ever registered as a bean — a bean of either type would satisfy
 * Spring Boot's own {@code @ConditionalOnMissingBean(ObjectMapper.class)} /
 * {@code @ConditionalOnMissingBean(TaskScheduler.class)} on its own auto-configured mapper/default
 * {@code "taskScheduler"} and silently replace it for every controller / {@code @Scheduled} method
 * in the host app. {@link ConfigClient#stop()} shuts the scheduler down itself since it's no longer
 * a container-managed bean.
 *
 * <p>Inactive entirely — no beans registered, no startup cost — unless {@code config-sdk.api-url}
 * is set to a non-blank value, so simply having this starter on the classpath is never itself a
 * source of failure. Host apps commonly wire this via {@code ${CONFIG_SDK_API_URL:}}, which
 * resolves to an empty string (not an absent property) when the env var isn't set — a bare
 * {@code @ConditionalOnProperty} presence check isn't enough, hence {@link
 * ApiUrlConfiguredCondition}.
 */
@AutoConfiguration
@EnableConfigurationProperties(ConfigSdkProperties.class)
@Conditional(ConfigSdkAutoConfiguration.ApiUrlConfiguredCondition.class)
public class ConfigSdkAutoConfiguration {

  private static ObjectMapper buildObjectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule());
  }

  private static TaskScheduler buildTaskScheduler(final ConfigSdkProperties props) {
    final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(Math.max(1, props.getNamespaces().size()));
    scheduler.setThreadNamePrefix("config-sdk-poller-");
    scheduler.initialize();
    return scheduler;
  }

  @Bean
  @ConditionalOnMissingBean(name = "configRestClient")
  public RestClient configRestClient(final ConfigSdkProperties props) {
    // Spring Boot 3.3-compatible timeout builder; 3.4+ moved this to a different package.
    final ClientHttpRequestFactorySettings settings =
        ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofSeconds(2))
            .withReadTimeout(props.getBootstrapTimeout());

    final ObjectMapper configSdkObjectMapper = buildObjectMapper();

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

  @Bean
  @ConditionalOnMissingBean
  public DiskSnapshotStore diskSnapshotStore(final ConfigSdkProperties props) {
    return new DiskSnapshotStore(Paths.get(props.getSnapshotDir()), buildObjectMapper());
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigClient configClient(
      final ConfigSdkProperties props,
      final ConfigApiClient configApiClient,
      final DiskSnapshotStore diskSnapshotStore) {
    return new ConfigClient(
        props.getEnvironment(),
        props.getNamespaces(),
        props.getDefaultPollIntervalSeconds(),
        props.getBootstrapTimeout(),
        props.getMaxBackoff(),
        configApiClient,
        diskSnapshotStore,
        buildTaskScheduler(props),
        buildObjectMapper());
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

  /**
   * Matches only when {@code config-sdk.api-url} resolves to a non-blank value. Plain
   * {@code @ConditionalOnProperty} treats an empty-but-present property (e.g. {@code
   * ${CONFIG_SDK_API_URL:}} with the env var unset) as "set", which would activate this
   * autoconfiguration — and every bean below it — with no actual API URL to call.
   */
  static final class ApiUrlConfiguredCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
      return StringUtils.hasText(context.getEnvironment().getProperty("config-sdk.api-url"));
    }
  }
}
