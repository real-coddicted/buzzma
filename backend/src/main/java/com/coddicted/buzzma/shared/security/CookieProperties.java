package com.coddicted.buzzma.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {

  private boolean secure = true;

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(final boolean secure) {
    this.secure = secure;
  }
}
