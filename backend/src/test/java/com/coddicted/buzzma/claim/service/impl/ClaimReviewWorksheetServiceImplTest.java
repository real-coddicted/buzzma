package com.coddicted.buzzma.claim.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.config.ClaimReviewWorksheetProperties;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRepository;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRowRepository;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.report.excel.ClaimReviewReportColumns;
import com.coddicted.buzzma.storage.service.StorageService;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ClaimReviewWorksheetServiceImplTest {

  @Mock private ClaimReviewWorksheetProperties properties;
  @Mock private ClaimReviewWorksheetRepository worksheetRepository;
  @Mock private ClaimReviewWorksheetRowRepository rowRepository;
  @Mock private StorageService storageService;

  @Test
  void testUploadWorksheetReadsShiftedColumnsAfterExchangeProductInsert() throws Exception {
    final ClaimReviewWorksheetServiceImpl service =
        new ClaimReviewWorksheetServiceImpl(
            properties, worksheetRepository, rowRepository, storageService);

    final List<String> headers =
        ClaimReviewReportColumns.COLUMNS.stream().map(col -> col.header()).toList();
    final String[] dataValues = {
      "Camp",
      "C-1",
      "Brand",
      "Med",
      "Buy",
      "Prof",
      "AMAZON",
      "ORD-1",
      "2026-01-01",
      "250.50",
      "Widget",
      "CLM-1",
      "APPROVED",
      "88",
      "100.00",
      "Approved",
      "looks good"
    };
    final byte[] workbookBytes = buildWorkbook(headers, dataValues);

    final MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "review.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            workbookBytes);

    when(properties.getMaxFileSizeBytes()).thenReturn(10L * 1024 * 1024);
    when(properties.getBatchSize()).thenReturn(100);
    when(storageService.store(eq("claim-review-worksheets"), anyString(), anyString(), any()))
        .thenReturn("storage-key");
    when(worksheetRepository.save(any()))
        .thenReturn(ClaimReviewWorksheet.builder().id(UUID.randomUUID()).build());

    assertDoesNotThrow(
        () -> service.uploadWorksheet(BuzzmaUser.builder().id(UUID.randomUUID()).build(), file));

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<List<ClaimReviewWorksheetRow>> captor =
        ArgumentCaptor.forClass(List.class);
    org.mockito.Mockito.verify(rowRepository).saveAll(captor.capture());
    final ClaimReviewWorksheetRow persisted = captor.getValue().get(0);

    assertEquals("Camp", persisted.getCampaign());
    assertEquals("250.50", persisted.getOrderAmount());
    assertEquals("CLM-1", persisted.getClaimCode());
    assertEquals("APPROVED", persisted.getClaimStatus());
    assertEquals("88", persisted.getMatchScore());
    assertEquals("100.00", persisted.getAmountApproved());
    assertEquals("Approved", persisted.getBrandReview());
    assertEquals("looks good", persisted.getRemarks());
  }

  private static byte[] buildWorkbook(final List<String> headers, final String[] dataValues)
      throws Exception {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final Sheet sheet = workbook.createSheet("Claim Review");
      final Row headerRow = sheet.createRow(0);
      for (int i = 0; i < headers.size(); i++) {
        headerRow.createCell(i).setCellValue(headers.get(i));
      }
      final Row dataRow = sheet.createRow(1);
      for (int i = 0; i < dataValues.length; i++) {
        dataRow.createCell(i).setCellValue(dataValues[i]);
      }
      workbook.write(out);
      return out.toByteArray();
    }
  }
}
