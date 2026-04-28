package com.coddicted.buzzma.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties jwtProperties;

  public JwtService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  public String generateAccessToken(UUID userId) {
    return buildToken(userId, jwtProperties.getAccessSecret(), jwtProperties.getAccessExpiryMs());
  }

  public String generateRefreshToken(UUID userId) {
    return buildToken(userId, jwtProperties.getRefreshSecret(), jwtProperties.getRefreshExpiryMs());
  }

  public UUID validateAccessToken(String token) {
    return extractSubject(token, jwtProperties.getAccessSecret());
  }

  public UUID validateRefreshToken(String token) {
    return extractSubject(token, jwtProperties.getRefreshSecret());
  }

  private String buildToken(UUID userId, String secret, long expiryMs) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(userId.toString())
        .issuedAt(new Date(now))
        .expiration(new Date(now + expiryMs))
        .signWith(key)
        .compact();
  }

  private UUID extractSubject(String token, String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    String subject = claims.getSubject();
    try {
      return UUID.fromString(subject);
    } catch (IllegalArgumentException e) {
      throw new JwtException("Invalid UUID subject in token: " + subject);
    }
  }
}
