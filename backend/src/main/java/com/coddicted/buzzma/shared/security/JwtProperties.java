package com.coddicted.buzzma.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  private String accessSecret;
  private String refreshSecret;
  private long accessExpiryMs = 900_000L;
  private long refreshExpiryMs = 604_800_000L;

  public String getAccessSecret() {
    return accessSecret;
  }

  public void setAccessSecret(String accessSecret) {
    this.accessSecret = accessSecret;
  }

  public String getRefreshSecret() {
    return refreshSecret;
  }

  public void setRefreshSecret(String refreshSecret) {
    this.refreshSecret = refreshSecret;
  }

  public long getAccessExpiryMs() {
    return accessExpiryMs;
  }

  public void setAccessExpiryMs(long accessExpiryMs) {
    this.accessExpiryMs = accessExpiryMs;
  }

  public long getRefreshExpiryMs() {
    return refreshExpiryMs;
  }

  public void setRefreshExpiryMs(long refreshExpiryMs) {
    this.refreshExpiryMs = refreshExpiryMs;
  }
}
