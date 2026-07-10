package com.coddicted.buzzma.identity.service;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenService {

  void issue(UUID userId, String rawToken, Instant expiresAt);

  void rotateToken(String oldRawToken, String newRawToken, Instant newExpiresAt);

  void revoke(String rawToken);

  void revokeAll(UUID userId);
}
