package com.coddicted.buzzma.communications.email.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.email.outbox")
@Getter
@Setter
public class EmailOutboxProperties {

  private int workerPoolSize = 1;
}
