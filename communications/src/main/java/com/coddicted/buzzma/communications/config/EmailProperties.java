package com.coddicted.buzzma.communications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.email")
@Getter
@Setter
public class EmailProperties {

  private String fromAddress;
}
