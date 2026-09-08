package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.config.ClaimReviewWorksheetProperties;
import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetDownloadDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRepository;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRowRepository;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.report.excel.ClaimReviewReportColumns;
import com.coddicted.buzzma.report.excel.WorkbookUtils;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClaimReviewWorksheetServiceImpl implements ClaimReviewWorksheetService {

  private static final int EXPECTED_COLUMN_COUNT = 17;

  private final ClaimReviewWorksheetProperties properties;
  private final ClaimReviewWorksheetRepository worksheetRepository;
  private final ClaimReviewWorksheetRowRepository rowRepository;
  private final StorageService storageService;

  public ClaimReviewWorksheetServiceImpl(
      final ClaimReviewWorksheetProperties properties,
      final ClaimReviewWorksheetRepository worksheetRepository,
      final ClaimReviewWorksheetRowRepository rowRepository,
      final StorageService storageService) {
    this.properties = properties;
    this.worksheetRepository = worksheetRepository;
    this.rowRepository = rowRepository;
    this.storageService = storageService;
  }

  @Override
  @Transactional
  public ClaimReviewWorksheet uploadWorksheet(final BuzzmaUser uploader, final MultipartFile file) {
    if (file.getSize() > properties.getMaxFileSizeBytes()) {
      throw new ResponseStatusException(
          HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds maximum allowed size");
    }

    final byte[] bytes = WorkbookUtils.readBytes(file);
    final Sheet sheet = WorkbookUtils.openFirstSheet(bytes);

    final List<String> expectedHeaders =
        ClaimReviewReportColumns.COLUMNS.stream()
            .map(col -> col.header())
            .limit(EXPECTED_COLUMN_COUNT)
            .toList();
    WorkbookUtils.validateHeaders(sheet, expectedHeaders);

    final String storageKey =
        storageService.store(
            "claim-review-worksheets", file.getOriginalFilename(), file.getContentType(), bytes);

    final int dataRowCount = WorkbookUtils.countDataRows(sheet, EXPECTED_COLUMN_COUNT);

    final ClaimReviewWorksheet worksheet =
        worksheetRepository.save(
            ClaimReviewWorksheet.builder()
                .uploadedBy(uploader.getId())
                .originalFilename(file.getOriginalFilename())
                .storageKey(storageKey)
                .rowCount(dataRowCount)
                .status(WorksheetRowStatus.PENDING)
                .build());

    persistRows(sheet, worksheet.getId());

    return worksheet;
  }

  @Override
  public List<ClaimReviewWorksheetResponseDto> listWorkbooks(final BuzzmaUser currentUser) {
    final List<ClaimReviewWorksheet> worksheets =
        worksheetRepository.findByUploadedByOrderByCreatedAtDesc(currentUser.getId());
    if (worksheets.isEmpty()) {
      return List.of();
    }

    final List<UUID> ids = worksheets.stream().map(ClaimReviewWorksheet::getId).toList();
    final Map<UUID, Long> processedCounts =
        rowRepository
            .countProcessedRowsGroupedByWorksheetId(
                ids, List.of(WorksheetRowStatus.SUCCESS, WorksheetRowStatus.ERROR))
            .stream()
            .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));

    return worksheets.stream()
        .map(
            ws ->
                ClaimReviewWorksheetResponseDto.builder()
                    .id(ws.getId())
                    .originalFilename(ws.getOriginalFilename())
                    .rowCount(ws.getRowCount())
                    .rowsProcessed(processedCounts.getOrDefault(ws.getId(), 0L).intValue())
                    .status(ws.getStatus())
                    .createdAt(ws.getCreatedAt())
                    .build())
        .toList();
  }

  @Override
  public ClaimReviewWorksheetDownloadDto downloadWorksheet(final UUID id) {
    final ClaimReviewWorksheet worksheet =
        worksheetRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worksheet not found"));
    final byte[] bytes = storageService.retrieve(worksheet.getStorageKey()).asByteArray();
    return new ClaimReviewWorksheetDownloadDto(worksheet.getOriginalFilename(), bytes);
  }

  private void persistRows(final Sheet sheet, final UUID worksheetId) {
    final int batchSize = properties.getBatchSize();
    final List<ClaimReviewWorksheetRow> batch = new ArrayList<>(batchSize);
    ClaimReviewWorksheetRow worksheetRow;
    final Map<String, String> worksheetRowIdMap = new HashMap<>();

    for (int r = 1; r <= sheet.getLastRowNum(); r++) {
      final Row row = sheet.getRow(r);
      if (!WorkbookUtils.isDataRow(row, EXPECTED_COLUMN_COUNT)) {
        continue;
      }
      worksheetRow = toRowEntity(row, worksheetId);
      checkForDuplicateRow(worksheetRowIdMap, worksheetRow, batch);
      batch.add(worksheetRow);
      if (batch.size() == batchSize) {
        List<ClaimReviewWorksheetRow> savedRows = rowRepository.saveAll(batch);
        savedRows.forEach(
            savedRow ->
                worksheetRowIdMap.put(savedRow.getClaimCode(), savedRow.getId().toString()));
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      rowRepository.saveAll(batch);
    }
  }

  private void checkForDuplicateRow(
      Map<String, String> worksheetRowNumberMap,
      ClaimReviewWorksheetRow worksheetRow,
      List<ClaimReviewWorksheetRow> batch) {
    if (worksheetRowNumberMap.containsKey(worksheetRow.getClaimCode())) {
      ClaimReviewWorksheetRow oldRow =
          rowRepository.getReferenceById(
              UUID.fromString(worksheetRowNumberMap.get(worksheetRow.getClaimCode())));
      markRowForError(oldRow);
      batch.add(oldRow);
      markRowForError(worksheetRow);
    }
  }

  @Override
  public UUID getUploadedBy(final UUID worksheetId) {
    return worksheetRepository
        .findById(worksheetId)
        .orElseThrow(() -> new NotFoundException("Worksheet not found: " + worksheetId))
        .getUploadedBy();
  }

  @Override
  @Transactional
  public void updateStatus(final UUID worksheetId, final WorksheetRowStatus status) {
    worksheetRepository.updateStatus(worksheetId, status);
  }

  private static void markRowForError(ClaimReviewWorksheetRow worksheetRow) {
    worksheetRow.setProcessingStatus(WorksheetRowStatus.ERROR);
    worksheetRow.setErrorRemarks("Duplicate worksheet entry");
  }

  /**
   * Reads cells back by position in the exact order of {@link ClaimReviewReportColumns#COLUMNS}.
   * The running {@code col} counter is advanced once per column so that inserting or removing a
   * column in that list only means adding/removing the matching line here — no block of indices to
   * renumber. Columns present in the export but not stored on the row (see the skip below) still
   * consume a position and must be stepped over explicitly.
   */
  private ClaimReviewWorksheetRow toRowEntity(final Row row, final UUID worksheetId) {
    int col = 0;
    final ClaimReviewWorksheetRow.ClaimReviewWorksheetRowBuilder builder =
        ClaimReviewWorksheetRow.builder()
            .worksheetId(worksheetId)
            .campaign(WorkbookUtils.cellString(row, col++))
            .campaignCode(WorkbookUtils.cellString(row, col++))
            .brand(WorkbookUtils.cellString(row, col++))
            .mediator(WorkbookUtils.cellString(row, col++))
            .buyer(WorkbookUtils.cellString(row, col++))
            .profileName(WorkbookUtils.cellString(row, col++))
            .platform(WorkbookUtils.cellString(row, col++))
            .orderId(WorkbookUtils.cellString(row, col++))
            .orderDate(WorkbookUtils.cellString(row, col++))
            .orderAmount(WorkbookUtils.cellString(row, col++));

    // Step over "Exchange Product" (ClaimReviewReportColumns index 10). It is written to the export
    // for display only and is never read back from a re-uploaded worksheet, so no builder field
    // consumes this position — but the counter must still advance to keep the columns below
    // aligned.
    col++;

    return builder
        .claimCode(WorkbookUtils.cellString(row, col++))
        .claimStatus(WorkbookUtils.cellString(row, col++))
        .matchScore(WorkbookUtils.cellString(row, col++))
        .amountApproved(WorkbookUtils.cellString(row, col++))
        .brandReview(WorkbookUtils.cellString(row, col++))
        .remarks(WorkbookUtils.cellString(row, col++))
        .processingStatus(WorksheetRowStatus.PENDING)
        .build();
  }
}
