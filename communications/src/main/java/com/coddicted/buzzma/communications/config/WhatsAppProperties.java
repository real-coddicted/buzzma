package com.coddicted.buzzma.communications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.whatsapp")
@Getter
@Setter
public class WhatsAppProperties {

  private String accessToken;
  private String phoneNumberId;
  private String verifyToken;
  private String appSecret;
  private String apiVersion;
  private String graphBaseUrl;
}
