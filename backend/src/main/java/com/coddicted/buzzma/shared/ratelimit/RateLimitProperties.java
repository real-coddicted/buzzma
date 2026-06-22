package com.coddicted.buzzma.shared.ratelimit;

import java.time.Duration;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

  private Auth auth = new Auth();
  private User user = new User();

  public void setAuth(final Auth auth) {
    this.auth = auth;
  }

  public void setUser(final User user) {
    this.user = user;
  }

  @Getter
  public static class Auth {
    private int capacity = 10;
    private int refillTokens = 10;
    private Duration refillPeriod = Duration.ofMinutes(1);
    private Duration expireAfterAccess = Duration.ofMinutes(5);

    public void setCapacity(final int capacity) {
      this.capacity = capacity;
    }

    public void setRefillTokens(final int refillTokens) {
      this.refillTokens = refillTokens;
    }

    public void setRefillPeriod(final Duration refillPeriod) {
      this.refillPeriod = refillPeriod;
    }

    public void setExpireAfterAccess(final Duration expireAfterAccess) {
      this.expireAfterAccess = expireAfterAccess;
    }
  }

  @Getter
  public static class User {
    private int capacity = 10;
    private int refillTokens = 10;
    private Duration refillPeriod = Duration.ofSeconds(1);
    private Duration expireAfterAccess = Duration.ofHours(1);

    public void setCapacity(final int capacity) {
      this.capacity = capacity;
    }

    public void setRefillTokens(final int refillTokens) {
      this.refillTokens = refillTokens;
    }

    public void setRefillPeriod(final Duration refillPeriod) {
      this.refillPeriod = refillPeriod;
    }

    public void setExpireAfterAccess(final Duration expireAfterAccess) {
      this.expireAfterAccess = expireAfterAccess;
    }
  }
}
