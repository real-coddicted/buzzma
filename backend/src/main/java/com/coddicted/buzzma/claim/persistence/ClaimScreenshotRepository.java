package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimScreenshotRepository extends JpaRepository<ClaimScreenshot, UUID> {

  List<ClaimScreenshot> findByClaimIdAndIsDeletedFalse(UUID claimId);

  List<ClaimScreenshot> findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID claimId);

  @Modifying
  @Query("UPDATE ClaimScreenshot s SET s.publicUrl = :url WHERE s.id = :id")
  int updatePublicUrl(@Param("id") UUID id, @Param("url") String url);
}
