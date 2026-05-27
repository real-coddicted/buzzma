package com.coddicted.buzzma.notifications.security;

import io.jsonwebtoken.JwtException;
import java.util.Collections;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JwtReactiveAuthenticationManager.class);

  private final JwtService jwtService;

  public JwtReactiveAuthenticationManager(final JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public Mono<Authentication> authenticate(final Authentication authentication) {
    final String token = authentication.getCredentials().toString();
    return Mono.fromCallable(() -> this.jwtService.validateAccessToken(token))
        .map(
            userId ->
                (Authentication)
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()))
        .onErrorMap(
            ex -> ex instanceof JwtException || ex instanceof IllegalArgumentException,
            ex -> {
              LOGGER.warn("JWT validation failed: {}", ex.getClass().getSimpleName());
              return new BadCredentialsException("Invalid JWT token");
            });
  }

  public static UUID userIdOf(final Authentication authentication) {
    return (UUID) authentication.getPrincipal();
  }
}
