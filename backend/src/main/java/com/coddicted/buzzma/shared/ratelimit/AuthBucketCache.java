package com.coddicted.buzzma.shared.ratelimit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

// IP-keyed: auth endpoints and unauthenticated requests
@Component
public class AuthBucketCache {

  private final LoadingCache<String, Bucket> cache;

  public AuthBucketCache(final RateLimitProperties props) {
    final RateLimitProperties.Auth cfg = props.getAuth();
    this.cache =
        Caffeine.newBuilder()
            .expireAfterAccess(cfg.getExpireAfterAccess())
            .build(
                key ->
                    Bucket.builder()
                        .addLimit(
                            Bandwidth.builder()
                                .capacity(cfg.getCapacity())
                                .refillGreedy(cfg.getRefillTokens(), cfg.getRefillPeriod())
                                .build())
                        .build());
  }

  public Bucket get(final String key) {
    return cache.get(key);
  }
}
