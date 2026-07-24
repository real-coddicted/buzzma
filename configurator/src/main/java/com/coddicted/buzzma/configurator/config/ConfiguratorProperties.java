package com.coddicted.buzzma.configurator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "configurator")
@Getter
@Setter
public class ConfiguratorProperties {

  private int pollIntervalSeconds = 45;
  private int bulkFetchCacheTtlSeconds = 5;
}
