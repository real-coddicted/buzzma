package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimScreenshotRepository extends JpaRepository<ClaimScreenshot, UUID> {

  List<ClaimScreenshot> findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID claimId);
}
