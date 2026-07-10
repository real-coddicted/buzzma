package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  void deleteByTokenHash(String tokenHash);

  void deleteByUserId(UUID userId);

  void deleteByUserIdAndExpiresAtBefore(UUID userId, Instant now);
}
