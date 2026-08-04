package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimReviewWorksheetRepository extends JpaRepository<ClaimReviewWorksheet, UUID> {

  List<ClaimReviewWorksheet> findByUploadedByOrderByCreatedAtDesc(UUID uploadedBy);
}
