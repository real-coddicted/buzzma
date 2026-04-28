package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.InviteEntity;
import com.coddicted.buzzma.shared.enums.InviteStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InviteRepository extends JpaRepository<InviteEntity, UUID> {

  Optional<InviteEntity> findByCode(String code);

  boolean existsByCode(String code);

  Page<InviteEntity> findAll(Pageable pageable);

  @Modifying
  @Query(
      "UPDATE InviteEntity i SET i.useCount = :newUseCount, i.status = :newStatus,"
          + " i.usedBy = :usedBy, i.usedAt = :usedAt, i.uses = :uses"
          + " WHERE i.code = :code AND i.status = 'active' AND i.useCount < :maxUses")
  int consumeInvite(
      @Param("code") String code,
      @Param("newUseCount") int newUseCount,
      @Param("newStatus") InviteStatus newStatus,
      @Param("usedBy") UUID usedBy,
      @Param("usedAt") Instant usedAt,
      @Param("uses") String uses,
      @Param("maxUses") int maxUses);

  @Modifying
  @Query(
      "UPDATE InviteEntity i SET i.status = 'revoked', i.revokedBy = :revokedBy,"
          + " i.revokedAt = :revokedAt WHERE i.code = :code AND i.status = 'active'")
  int revokeInvite(
      @Param("code") String code,
      @Param("revokedBy") UUID revokedBy,
      @Param("revokedAt") Instant revokedAt);
}
