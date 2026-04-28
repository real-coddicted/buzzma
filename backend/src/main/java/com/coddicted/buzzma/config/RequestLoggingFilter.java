package com.coddicted.buzzma.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final int MAX_PAYLOAD_LENGTH = 2048;

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/health")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs");
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    if (!logger.isInfoEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    ContentCachingRequestWrapper wrappedRequest = wrapRequest(request);
    ContentCachingResponseWrapper wrappedResponse = wrapResponse(response);
    long startNanos = System.nanoTime();

    if (logger.isDebugEnabled()) {
      logger.debug(
          String.format(
              "HTTP REQUEST IN: method=%s, uri=%s, client=%s, contentType=%s, authHeader=%s, userAgent=%s",
              wrappedRequest.getMethod(),
              buildRequestTarget(wrappedRequest),
              wrappedRequest.getRemoteAddr(),
              nullSafe(wrappedRequest.getContentType()),
              describeAuthHeader(wrappedRequest.getHeader("Authorization")),
              nullSafe(wrappedRequest.getHeader("User-Agent"))));
    }

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } finally {
      long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
      String requestTarget = buildRequestTarget(wrappedRequest);
      String requestBody = extractRequestBody(wrappedRequest);
      String responseBody = extractResponseBody(wrappedResponse);
      String principal = describePrincipal();

      String message =
          String.format(
              "HTTP EXCHANGE: method=%s, uri=%s, client=%s, status=%d, durationMs=%d, principal=%s, requestBody=%s, responseBody=%s",
              wrappedRequest.getMethod(),
              requestTarget,
              wrappedRequest.getRemoteAddr(),
              wrappedResponse.getStatus(),
              durationMs,
              principal,
              requestBody,
              responseBody);
      logger.info(message);

      if (logger.isDebugEnabled()) {
        logger.debug(
            String.format(
                "HTTP RESPONSE OUT: status=%d, contentType=%s, contentLength=%d",
                wrappedResponse.getStatus(),
                nullSafe(wrappedResponse.getContentType()),
                wrappedResponse.getContentSize()));
      }

      wrappedResponse.copyBodyToResponse();
    }
  }

  private String describePrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      return "anonymous";
    }
    String authorities =
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining("|"));
    return auth.getName() + "[" + (authorities.isEmpty() ? "no-authorities" : authorities) + "]";
  }

  private String describeAuthHeader(String header) {
    if (header == null || header.isBlank()) {
      return "absent";
    }
    int space = header.indexOf(' ');
    String scheme = space > 0 ? header.substring(0, space) : header;
    return scheme + " <redacted>";
  }

  private String nullSafe(String value) {
    return value == null ? "-" : value;
  }

  private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
    if (request instanceof ContentCachingRequestWrapper wrapper) {
      return wrapper;
    }
    return new ContentCachingRequestWrapper(request);
  }

  private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
    if (response instanceof ContentCachingResponseWrapper wrapper) {
      return wrapper;
    }
    return new ContentCachingResponseWrapper(response);
  }

  private String buildRequestTarget(HttpServletRequest request) {
    String query = request.getQueryString();
    if (query == null || query.isBlank()) {
      return request.getRequestURI();
    }
    return request.getRequestURI() + "?" + query;
  }

  private String extractRequestBody(ContentCachingRequestWrapper request) {
    return extractBody(
        request.getContentAsByteArray(), request.getContentType(), request.getCharacterEncoding());
  }

  private String extractResponseBody(ContentCachingResponseWrapper response) {
    return extractBody(
        response.getContentAsByteArray(),
        response.getContentType(),
        response.getCharacterEncoding());
  }

  private String extractBody(byte[] body, String contentType, String encoding) {
    if (body == null || body.length == 0) {
      return "-";
    }
    if (!isTextContentType(contentType)) {
      return "[binary content omitted]";
    }

    int length = Math.min(body.length, MAX_PAYLOAD_LENGTH);
    Charset charset = resolveCharset(encoding);
    String content = new String(body, 0, length, charset).replaceAll("\\s+", " ").trim();

    if (body.length > MAX_PAYLOAD_LENGTH) {
      return content + "... [truncated]";
    }
    return content;
  }

  private boolean isTextContentType(String contentType) {
    if (contentType == null) {
      return false;
    }

    String value = contentType.toLowerCase();
    return value.startsWith("text/")
        || value.contains("json")
        || value.contains("xml")
        || value.contains("x-www-form-urlencoded");
  }

  private Charset resolveCharset(String encoding) {
    if (encoding == null || encoding.isBlank()) {
      return StandardCharsets.UTF_8;
    }
    try {
      return Charset.forName(encoding);
    } catch (Exception ignored) {
      return StandardCharsets.UTF_8;
    }
  }
}
