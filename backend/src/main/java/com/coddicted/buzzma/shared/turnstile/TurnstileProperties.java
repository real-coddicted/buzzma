package com.coddicted.buzzma.shared.turnstile;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.turnstile")
public class TurnstileProperties {

  private boolean enabled = true;
  private String secretKey;
  private String verifyUrl = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getVerifyUrl() {
    return verifyUrl;
  }

  public void setVerifyUrl(String verifyUrl) {
    this.verifyUrl = verifyUrl;
  }
}
