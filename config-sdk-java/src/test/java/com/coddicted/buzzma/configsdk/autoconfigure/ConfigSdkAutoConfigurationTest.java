package com.coddicted.buzzma.configsdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.coddicted.buzzma.configsdk.ConfigClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ConfigSdkAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ConfigSdkAutoConfiguration.class));

  @Test
  void noBeansRegisteredWhenApiUrlNotConfigured() {
    contextRunner.run(context -> assertThat(context).doesNotHaveBean(ConfigClient.class));
  }

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
