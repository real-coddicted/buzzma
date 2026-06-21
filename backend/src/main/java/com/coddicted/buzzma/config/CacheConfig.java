package com.coddicted.buzzma.config;

import com.coddicted.buzzma.shared.constants.WellKnownCaches;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    final CaffeineCacheManager manager =
        new CaffeineCacheManager(WellKnownCaches.SECURITY_QUESTIONS_CACHE);
    manager.setCaffeine(
        Caffeine.newBuilder().maximumSize(20).expireAfterWrite(Duration.ofHours(1)));
    return manager;
  }
}
