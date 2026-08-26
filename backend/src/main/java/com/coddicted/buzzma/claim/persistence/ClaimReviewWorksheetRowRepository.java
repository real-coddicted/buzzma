package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ClaimReviewWorksheetRowRepository
    extends JpaRepository<ClaimReviewWorksheetRow, UUID> {

  @Query(
      value =
          """
          SELECT r FROM ClaimReviewWorksheetRow r
          WHERE r.worksheetId = :worksheetId
            AND r.worksheetId IN (SELECT w.id FROM ClaimReviewWorksheet w WHERE w.uploadedBy = :uploadedBy)
            AND (:statuses IS NULL OR r.processingStatus IN :statuses)
          """,
      countQuery =
          """
          SELECT COUNT(r) FROM ClaimReviewWorksheetRow r
          WHERE r.worksheetId = :worksheetId
            AND r.worksheetId IN (SELECT w.id FROM ClaimReviewWorksheet w WHERE w.uploadedBy = :uploadedBy)
            AND (:statuses IS NULL OR r.processingStatus IN :statuses)
          """)
  Page<ClaimReviewWorksheetRow> findByWorksheetIdAndUploadByAndStatuses(
      @Param("worksheetId") UUID worksheetId,
      @Param("uploadedBy") UUID uploadedBy,
      @Param("statuses") Collection<WorksheetRowStatus> statuses,
      Pageable pageable);

  @Query(
      "SELECT r.worksheetId, COUNT(r) FROM ClaimReviewWorksheetRow r"
          + " WHERE r.worksheetId IN :worksheetIds AND r.processingStatus IN :statuses"
          + " GROUP BY r.worksheetId")
  List<Object[]> countProcessedRowsGroupedByWorksheetId(
      @Param("worksheetIds") Collection<UUID> worksheetIds,
      @Param("statuses") Collection<WorksheetRowStatus> statuses);

  @Modifying
  @Transactional
  @Query(
      nativeQuery = true,
      value =
          """
      UPDATE claim_review_worksheet_rows
      SET processing_status = 'PENDING'
      WHERE processing_status = 'IN_PROGRESS'
        AND last_attempted_at < NOW() - (INTERVAL '1 minute' * :thresholdMinutes)
      """)
  int resetStaleInProgressRows(@Param("thresholdMinutes") int thresholdMinutes);

  @Modifying
  @Transactional
  @Query(
      nativeQuery = true,
      value =
          """
      UPDATE claim_review_worksheet_rows
      SET processing_status = 'PENDING',
          retry_count       = retry_count + 1
      WHERE id = :rowId
      """)
  void resetForRetry(@Param("rowId") UUID rowId);

  @Modifying
  @Transactional
  @Query(
      nativeQuery = true,
      value =
          """
      UPDATE claim_review_worksheet_rows
      SET processing_status = 'ERROR',
          retry_count       = retry_count + 1,
          error_remarks     = :errorRemarks
      WHERE id = :rowId
      """)
  void markFailed(@Param("rowId") UUID rowId, @Param("errorRemarks") String errorRemarks);

  @Modifying
  @Transactional
  @Query(
      nativeQuery = true,
      value =
          """
      UPDATE claim_review_worksheet_rows
      SET processing_status = 'SUCCESS'
      WHERE id = :rowId
      """)
  void markSuccess(@Param("rowId") UUID rowId);

  @Query(
      nativeQuery = true,
      value =
          """
      SELECT COUNT(*) = 0 FROM claim_review_worksheet_rows
      WHERE worksheet_id = :worksheetId
        AND processing_status NOT IN ('SUCCESS', 'ERROR')
      """)
  boolean allRowsTerminalForWorksheet(@Param("worksheetId") UUID worksheetId);

  @Query(
      nativeQuery = true,
      value =
          """
      SELECT COUNT(*) > 0 FROM claim_review_worksheet_rows
      WHERE worksheet_id = :worksheetId
        AND processing_status = 'ERROR'
      """)
  boolean anyRowFailedForWorksheet(@Param("worksheetId") UUID worksheetId);
}
