package com.coddicted.buzzma.configsdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.coddicted.buzzma.configsdk.ConfigClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.EnableScheduling;

class ConfigSdkAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ConfigSdkAutoConfiguration.class));

  @Test
  void noBeansRegisteredWhenApiUrlNotConfigured() {
    contextRunner.run(context -> assertThat(context).doesNotHaveBean(ConfigClient.class));
  }

  @Test
  void noBeansRegisteredWhenApiUrlResolvesToEmptyString() {
    // Reproduces host apps commonly wiring this via `${CONFIG_SDK_API_URL:}` — an unset env var
    // resolves to an empty-but-present property, which a bare @ConditionalOnProperty would treat
    // as "set" and wrongly activate the whole autoconfiguration.
    contextRunner
        .withPropertyValues("config-sdk.api-url=")
        .run(context -> assertThat(context).doesNotHaveBean(ConfigClient.class));
  }

  @Test
  void doesNotShadowHostAppsObjectMapperBeanEvenWhenActive() {
    // Reproduces the host app's own JacksonAutoConfiguration losing its
    // @ConditionalOnMissingBean(ObjectMapper.class) race to config-sdk's ObjectMapper — which,
    // before configSdkObjectMapper() stopped being a @Bean, had WRITE_DATES_AS_TIMESTAMPS enabled
    // (Jackson's bare default) and silently took over every controller's date serialization.
    // Config-sdk is deliberately active here (a real api-url is set) — the bug reproduces
    // regardless of whether CONFIG_SDK_API_URL is configured or left unset.
    new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(ConfigSdkAutoConfiguration.class, JacksonAutoConfiguration.class))
        .withPropertyValues(
            "config-sdk.api-url=https://config-api.invalid",
            "config-sdk.environment=prod",
            "config-sdk.namespaces[0]=checkout-service",
            // Kept short so context refresh (which starts the SmartLifecycle bean) doesn't hang
            // on an unreachable host — only the ObjectMapper bean identity is asserted on here.
            "config-sdk.bootstrap-timeout=100ms")
        .run(
            context -> {
              assertThat(context).hasSingleBean(ObjectMapper.class);
              assertThat(
                      context
                          .getBean(ObjectMapper.class)
                          .getSerializationConfig()
                          .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
                  .isFalse();
            });
  }

  @Test
  void doesNotShadowHostAppsDefaultTaskSchedulerBeanEvenWhenActive() {
    // Reproduces @Scheduled jobs in a host app with @EnableScheduling silently being routed onto
    // config-sdk's own dedicated poller scheduler — which, before configSdkTaskScheduler() stopped
    // being a @Bean, satisfied Spring Boot's @ConditionalOnMissingBean(TaskScheduler.class) on its
    // default "taskScheduler" bean, so Boot's own default scheduler was never created and
    // ScheduledAnnotationBeanPostProcessor fell back to autodetecting the sole remaining
    // TaskScheduler bean in the context: config-sdk's, sized only for its own namespace count.
    new ApplicationContextRunner()
        .withUserConfiguration(SchedulingEnabledConfig.class)
        .withConfiguration(
            AutoConfigurations.of(
                ConfigSdkAutoConfiguration.class, TaskSchedulingAutoConfiguration.class))
        .withPropertyValues(
            "config-sdk.api-url=https://config-api.invalid",
            "config-sdk.environment=prod",
            "config-sdk.namespaces[0]=checkout-service",
            // Kept short so context refresh (which starts the SmartLifecycle bean) doesn't hang
            // on an unreachable host — only the default scheduler bean's presence is asserted on.
            "config-sdk.bootstrap-timeout=100ms")
        .run(context -> assertThat(context).hasBean("taskScheduler"));
  }

  @EnableScheduling
  static class SchedulingEnabledConfig {}

  @Test
  void configClientBeanCreatedAndWiredWhenPropertiesSet() {
    contextRunner
        .withPropertyValues(
            "config-sdk.api-url=https://config-api.invalid",
            "config-sdk.environment=prod",
            "config-sdk.namespaces[0]=checkout-service",
            // Kept short so context refresh (which starts the SmartLifecycle bean) doesn't hang
            // on an unreachable host — the fallback chain is exercised, not asserted on, here.
            "config-sdk.bootstrap-timeout=100ms")
        .run(
            context -> {
              assertThat(context).hasSingleBean(ConfigClient.class);
              assertThat(context.getBean(ConfigClient.class).forNamespace("checkout-service"))
                  .isNotNull();
            });
  }
}
