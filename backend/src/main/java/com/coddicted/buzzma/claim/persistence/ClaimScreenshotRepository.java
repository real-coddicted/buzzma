package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.dto.PendingR2UploadView;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimScreenshotRepository extends JpaRepository<ClaimScreenshot, UUID> {

  List<ClaimScreenshot> findByClaimIdAndIsDeletedFalse(UUID claimId);

  List<ClaimScreenshot> findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID claimId);

  List<ClaimScreenshot> findByClaimIdInAndIsDeletedFalseOrderByCreatedAtAsc(
      Collection<UUID> claimIds);

  @Modifying
  @Query("UPDATE ClaimScreenshot s SET s.publicUrl = :url WHERE s.id = :id")
  int updatePublicUrl(@Param("id") UUID id, @Param("url") String url);

  @Modifying
  @Query(
      "UPDATE ClaimScreenshot s SET s.r2UploadAttempts = s.r2UploadAttempts + 1 WHERE s.id = :id")
  int incrementR2UploadAttempts(@Param("id") UUID id);

  @Query("SELECT s.r2UploadAttempts FROM ClaimScreenshot s WHERE s.id = :id")
  Optional<Integer> findR2UploadAttempts(@Param("id") UUID id);

  @Query(
      """
      SELECT new com.coddicted.buzzma.claim.dto.PendingR2UploadView(
          s.id, s.claimId, c.code, c.campaignId, s.storageKey, s.type)
      FROM ClaimScreenshot s JOIN Claim c ON c.id = s.claimId
      WHERE s.publicUrl IS NULL
        AND s.isDeleted = false
        AND c.isDeleted = false
        AND s.createdAt < :createdBefore
        AND s.r2UploadAttempts < :maxAttempts
      ORDER BY s.createdAt ASC
      """)
  List<PendingR2UploadView> findPendingR2Uploads(
      @Param("createdBefore") Instant createdBefore,
      @Param("maxAttempts") int maxAttempts,
      Pageable pageable);
}
