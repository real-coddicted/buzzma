package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.RefreshToken;
import com.coddicted.buzzma.identity.persistence.RefreshTokenRepository;
import com.coddicted.buzzma.identity.service.RefreshTokenService;
import com.coddicted.buzzma.shared.exception.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshTokenServiceImpl(final RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  @Transactional
  public void issue(final UUID userId, final String rawToken, final Instant expiresAt) {
    // This logs out any other sessions signed in on other device/ browser(s)
    this.refreshTokenRepository.deleteByUserId(userId);
    this.refreshTokenRepository.save(
        RefreshToken.builder()
            .userId(userId)
            .tokenHash(hash(rawToken))
            .expiresAt(expiresAt)
            .build());
  }

  @Override
  @Transactional
  public void rotateToken(
      final String oldRawToken, final String newRawToken, final Instant newExpiresAt) {
    final RefreshToken existing =
        this.refreshTokenRepository
            .findByTokenHash(hash(oldRawToken))
            .orElseThrow(
                () -> new UnauthorizedException("Refresh token not found or already used"));
    if (Instant.now().isAfter(existing.getExpiresAt())) {
      throw new UnauthorizedException("Refresh token expired");
    }
    final UUID userId = existing.getUserId();
    this.refreshTokenRepository.delete(existing);
    this.refreshTokenRepository.save(
        RefreshToken.builder()
            .userId(userId)
            .tokenHash(hash(newRawToken))
            .expiresAt(newExpiresAt)
            .build());
    this.refreshTokenRepository.deleteByUserIdAndExpiresAtBefore(userId, Instant.now());
  }

  @Override
  @Transactional
  public void revoke(final String rawToken) {
    this.refreshTokenRepository.deleteByTokenHash(hash(rawToken));
  }

  @Override
  @Transactional
  public void revokeAll(final UUID userId) {
    this.refreshTokenRepository.deleteByUserId(userId);
  }

  private String hash(final String rawToken) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      final StringBuilder hex = new StringBuilder(64);
      for (final byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
