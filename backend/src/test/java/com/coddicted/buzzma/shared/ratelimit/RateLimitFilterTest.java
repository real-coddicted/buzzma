package com.coddicted.buzzma.shared.ratelimit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;
  @Mock private PrintWriter writer;

  private RateLimitFilter filter;

  @BeforeEach
  void setUp() throws Exception {
    final RateLimitProperties props = new RateLimitProperties();
    filter = new RateLimitFilter(new AuthBucketCache(props), new UserBucketCache(props));
    // lenient: only used in tests that trigger 429
    lenient().when(response.getWriter()).thenReturn(writer);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // --- Auth path ---

  @Test
  void testAuthPathWithinLimitPassesThrough() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/sign-in");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("1.2.3.4");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testAuthPathOverLimitReturns429() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/sign-in");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("1.2.3.4");

    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }
    filter.doFilter(request, response, filterChain);

    verify(filterChain, times(10)).doFilter(request, response);
    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testAuthPathDifferentIpsHaveSeparateBuckets() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/sign-in");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);

    when(request.getRemoteAddr()).thenReturn("1.1.1.1");
    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }

    when(request.getRemoteAddr()).thenReturn("2.2.2.2");
    filter.doFilter(request, response, filterChain);

    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  // --- Authenticated path ---

  @Test
  void testAuthenticatedPathWithinLimitPassesThrough() throws Exception {
    setAuthenticatedUser("user1");
    when(request.getRequestURI()).thenReturn("/api/v1/campaigns");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testAuthenticatedPathOverLimitReturns429() throws Exception {
    setAuthenticatedUser("user1");
    when(request.getRequestURI()).thenReturn("/api/v1/claims");

    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }
    filter.doFilter(request, response, filterChain);

    verify(filterChain, times(10)).doFilter(request, response);
    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testAuthenticatedPathDifferentUsersHaveSeparateBuckets() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/campaigns");

    setAuthenticatedUser("user1");
    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }

    setAuthenticatedUser("user2");
    filter.doFilter(request, response, filterChain);

    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  // --- Unauthenticated non-auth paths ---

  @Test
  void testNoSecurityContextNonAuthPathPassesThrough() throws Exception {
    SecurityContextHolder.clearContext();
    when(request.getRequestURI()).thenReturn("/api/v1/campaigns");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("3.3.3.3");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testAnonymousAuthNonAuthPathPassesThrough() throws Exception {
    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    SecurityContextHolder.setContext(ctx);
    when(request.getRequestURI()).thenReturn("/api/v1/campaigns");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("4.4.4.4");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testAnonymousNonAuthPathOverLimitReturns429() throws Exception {
    SecurityContextHolder.clearContext();
    when(request.getRequestURI()).thenReturn("/api/v1/campaigns");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("5.5.5.5");

    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }
    filter.doFilter(request, response, filterChain);

    verify(filterChain, times(10)).doFilter(request, response);
    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testAnonymousNonAuthPathDifferentIpsHaveSeparateBuckets() throws Exception {
    SecurityContextHolder.clearContext();
    when(request.getRequestURI()).thenReturn("/api/v1/campaigns");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);

    when(request.getRemoteAddr()).thenReturn("6.6.6.6");
    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }

    when(request.getRemoteAddr()).thenReturn("7.7.7.7");
    filter.doFilter(request, response, filterChain);

    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  // --- IP extraction ---

  @Test
  void testIpExtractionPrefersCfConnectingIp() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/sign-in");

    when(request.getHeader("CF-Connecting-IP")).thenReturn("10.0.0.1");
    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }

    // Different CF-Connecting-IP — separate bucket, should still pass
    when(request.getHeader("CF-Connecting-IP")).thenReturn("10.0.0.2");
    filter.doFilter(request, response, filterChain);

    verify(filterChain, times(11)).doFilter(request, response);
    verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testIpExtractionFallsBackToXForwardedFor() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/sign-in");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");

    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }
    filter.doFilter(request, response, filterChain);

    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void testIpExtractionFallsBackToRemoteAddr() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/sign-in");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("10.0.0.5");

    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }
    filter.doFilter(request, response, filterChain);

    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  // --- 429 response shape ---

  @Test
  void testRateLimitedResponseHasCorrectShape() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/sign-in");
    when(request.getHeader("CF-Connecting-IP")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("9.9.9.9");

    for (int i = 0; i < 10; i++) {
      filter.doFilter(request, response, filterChain);
    }
    filter.doFilter(request, response, filterChain);

    verify(response).setStatus(429);
    verify(response).setContentType("application/json");
    verify(writer).write(contains("RATE_LIMIT_EXCEEDED"));

    final ArgumentCaptor<String> retryAfter = ArgumentCaptor.forClass(String.class);
    verify(response).setHeader(eq("Retry-After"), retryAfter.capture());
    assertTrue(Long.parseLong(retryAfter.getValue()) > 0);
  }

  private void setAuthenticatedUser(final String username) {
    final SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(new UsernamePasswordAuthenticationToken(username, null, List.of()));
    SecurityContextHolder.setContext(ctx);
  }
}
