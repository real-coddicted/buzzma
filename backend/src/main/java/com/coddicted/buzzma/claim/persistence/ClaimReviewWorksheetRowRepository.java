package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimReviewWorksheetRowRepository
    extends JpaRepository<ClaimReviewWorksheetRow, UUID> {

  @Query(
      "SELECT r.worksheetId, COUNT(r) FROM ClaimReviewWorksheetRow r"
          + " WHERE r.worksheetId IN :worksheetIds AND r.processingStatus IN :statuses"
          + " GROUP BY r.worksheetId")
  List<Object[]> countProcessedRowsGroupedByWorksheetId(
      @Param("worksheetIds") Collection<UUID> worksheetIds,
      @Param("statuses") Collection<WorksheetRowStatus> statuses);
}
