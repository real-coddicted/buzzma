package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ClaimReviewWorksheetRepository extends JpaRepository<ClaimReviewWorksheet, UUID> {

  List<ClaimReviewWorksheet> findByUploadedByOrderByCreatedAtDesc(UUID uploadedBy);

  @Modifying
  @Transactional
  @Query("UPDATE ClaimReviewWorksheet w SET w.status = :status WHERE w.id = :worksheetId")
  void updateStatus(
      @Param("worksheetId") UUID worksheetId, @Param("status") WorksheetRowStatus status);
}
