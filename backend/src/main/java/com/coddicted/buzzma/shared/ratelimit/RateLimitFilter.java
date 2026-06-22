package com.coddicted.buzzma.shared.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {

  private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";

  private final AuthBucketCache authBucketCache;
  private final UserBucketCache userBucketCache;

  public RateLimitFilter(
      final AuthBucketCache authBucketCache, final UserBucketCache userBucketCache) {
    this.authBucketCache = authBucketCache;
    this.userBucketCache = userBucketCache;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final @NonNull HttpServletResponse response,
      final @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String path = request.getRequestURI();
    final ConsumptionProbe probe;

    if (path.startsWith(AUTH_PATH_PREFIX)) {
      probe = authBucketCache.get(getClientIp(request)).tryConsumeAndReturnRemaining(1);
    } else {
      final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      final String name = auth != null ? auth.getName() : null;
      if (auth == null
          || auth instanceof AnonymousAuthenticationToken
          || !auth.isAuthenticated()
          || name == null) {
        probe = authBucketCache.get(getClientIp(request)).tryConsumeAndReturnRemaining(1);
      } else {
        probe = userBucketCache.get(name).tryConsumeAndReturnRemaining(1);
      }
    }

    if (!probe.isConsumed()) {
      final long retryAfter = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader("Retry-After", String.valueOf(retryAfter));
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response
          .getWriter()
          .write(
              "{\"error\":\"RATE_LIMIT_EXCEEDED\","
                  + "\"message\":\"Too many requests. Please try again later.\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String getClientIp(final HttpServletRequest request) {
    final String cfIp = request.getHeader("CF-Connecting-IP");
    if (cfIp != null && !cfIp.isBlank()) {
      return cfIp;
    }
    final String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
