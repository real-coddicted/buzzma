package com.coddicted.buzzma.notifications.security;

import com.coddicted.buzzma.notifications.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey accessKey;

  public JwtService(final JwtProperties jwtProperties) {
    this.accessKey =
        Keys.hmacShaKeyFor(jwtProperties.getAccessSecret().getBytes(StandardCharsets.UTF_8));
  }

  public UUID validateAccessToken(final String token) {
    final Claims claims =
        Jwts.parser().verifyWith(this.accessKey).build().parseSignedClaims(token).getPayload();
    final String subject = claims.getSubject();
    try {
      return UUID.fromString(subject);
    } catch (IllegalArgumentException e) {
      throw new JwtException("Invalid UUID subject in token: " + subject);
    }
  }
}
