package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimReviewWorksheetRowService {

  List<UUID> claimBatchForProcessing(int batchSize, int maxRetries);

  void processRow(ClaimReviewWorksheetRow row);

  void resetForRetry(UUID rowId);

  void markFailed(UUID rowId, String errorRemarks);

  Page<ClaimReviewWorksheetRow> listRows(
      UUID worksheetId, UUID uploadedBy, WorksheetRowStatus status, Pageable pageable);
}
