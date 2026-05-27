package com.coddicted.buzzma.notifications.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String TOKEN_QUERY_PARAM = "access_token";

  @Override
  public Mono<Authentication> convert(final ServerWebExchange exchange) {
    return Mono.justOrEmpty(extractToken(exchange))
        .map(token -> new UsernamePasswordAuthenticationToken(token, token));
  }

  private String extractToken(final ServerWebExchange exchange) {
    final String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    final String queryToken = exchange.getRequest().getQueryParams().getFirst(TOKEN_QUERY_PARAM);
    if (queryToken != null && !queryToken.isBlank()) {
      return queryToken;
    }
    return null;
  }
}
