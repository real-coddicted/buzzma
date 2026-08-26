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

  private static final int EXPECTED_COLUMN_COUNT = 16;

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

  private ClaimReviewWorksheetRow toRowEntity(final Row row, final UUID worksheetId) {
    return ClaimReviewWorksheetRow.builder()
        .worksheetId(worksheetId)
        .campaign(WorkbookUtils.cellString(row, 0))
        .campaignCode(WorkbookUtils.cellString(row, 1))
        .brand(WorkbookUtils.cellString(row, 2))
        .mediator(WorkbookUtils.cellString(row, 3))
        .buyer(WorkbookUtils.cellString(row, 4))
        .profileName(WorkbookUtils.cellString(row, 5))
        .platform(WorkbookUtils.cellString(row, 6))
        .orderId(WorkbookUtils.cellString(row, 7))
        .orderDate(WorkbookUtils.cellString(row, 8))
        .orderAmount(WorkbookUtils.cellString(row, 9))
        .claimCode(WorkbookUtils.cellString(row, 10))
        .claimStatus(WorkbookUtils.cellString(row, 11))
        .matchScore(WorkbookUtils.cellString(row, 12))
        .amountApproved(WorkbookUtils.cellString(row, 13))
        .brandReview(WorkbookUtils.cellString(row, 14))
        .remarks(WorkbookUtils.cellString(row, 15))
        .processingStatus(WorksheetRowStatus.PENDING)
        .build();
  }
}
