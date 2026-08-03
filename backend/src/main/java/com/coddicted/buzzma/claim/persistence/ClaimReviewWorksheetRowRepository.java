package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimReviewWorksheetRowRepository
    extends JpaRepository<ClaimReviewWorksheetRow, UUID> {}
