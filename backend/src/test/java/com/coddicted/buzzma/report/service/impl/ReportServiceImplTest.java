package com.coddicted.buzzma.report.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.dto.ClaimReviewFilterRequestDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.processor.ClaimReviewProcessor;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.report.excel.ExcelReportWriter;
import com.coddicted.buzzma.shared.enums.Platform;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

  @Mock private ClaimReviewProcessor claimReviewProcessor;

  @Test
  void testGenerateClaimReviewReportWritesExpectedColumnsAndRows() throws Exception {
    final BuzzmaUser agency =
        BuzzmaUser.builder().id(UUID.randomUUID()).role(UserRole.ROLE_AGENCY).build();
    final UUID campaignId = UUID.randomUUID();
    final ClaimReviewFilterRequestDto filter =
        ClaimReviewFilterRequestDto.builder().campaignIds(Set.of(campaignId)).build();
    final ClaimReviewResponseDto row =
        ClaimReviewResponseDto.builder()
            .campaignName("Summer Sale")
            .campaignCode("CMP-001")
            .brandName("Acme")
            .dealOwnerName("Mediator A")
            .dealOwnerCode("MED-001")
            .buyerName("Buyer A")
            .buyerCode("BUY-001")
            .platform(Platform.PLATFORM_AMAZON)
            .ecommerceOrderId("ORD-1")
            .orderDate(20260101)
            .claimStatus("CLAIM_STATUS_ACTIVE")
            .claimReviewStatus(ClaimReviewStatus.CLAIM_REVIEW_STATUS_APPROVED)
            .matchScore(BigInteger.valueOf(90))
            .mediatorVerified(true)
            .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
            .updatedAt(Instant.parse("2026-01-02T10:00:00Z"))
            .build();

    final ReportServiceImpl serviceWithMock =
        new ReportServiceImpl(claimReviewProcessor, new ExcelReportWriter());
    when(claimReviewProcessor.listClaimReviews(
            eq(agency), eq(Set.of(campaignId)), isNull(), isNull(), any(Pageable.class)))
        .thenReturn(pageOf(row));

    final byte[] bytes = serviceWithMock.generateClaimReviewReport(agency, filter);

    verify(claimReviewProcessor)
        .listClaimReviews(
            eq(agency), eq(Set.of(campaignId)), isNull(), isNull(), eq(Pageable.unpaged()));

    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
      final Sheet sheet = workbook.getSheet("Claim Review");
      final Row header = sheet.getRow(0);
      assertEquals("Campaign", header.getCell(0).getStringCellValue());
      assertEquals("Campaign Code", header.getCell(1).getStringCellValue());
      assertEquals("Mediator Code", header.getCell(4).getStringCellValue());
      assertEquals("Buyer Code", header.getCell(6).getStringCellValue());
      assertEquals("Order Date", header.getCell(9).getStringCellValue());

      final Row dataRow = sheet.getRow(1);
      assertEquals("Summer Sale", dataRow.getCell(0).getStringCellValue());
      assertEquals("CMP-001", dataRow.getCell(1).getStringCellValue());
      assertEquals("Acme", dataRow.getCell(2).getStringCellValue());
      assertEquals("MED-001", dataRow.getCell(4).getStringCellValue());
      assertEquals("BUY-001", dataRow.getCell(6).getStringCellValue());
      assertEquals("2026-01-01", dataRow.getCell(9).getStringCellValue());
    }
  }

  @Test
  void testGenerateClaimReviewReportWithNullFilterPassesNullCriteria() {
    final BuzzmaUser mediator =
        BuzzmaUser.builder().id(UUID.randomUUID()).role(UserRole.ROLE_MEDIATOR).build();
    final ReportServiceImpl serviceWithMock =
        new ReportServiceImpl(claimReviewProcessor, new ExcelReportWriter());
    when(claimReviewProcessor.listClaimReviews(
            eq(mediator), isNull(), isNull(), isNull(), any(Pageable.class)))
        .thenReturn(Page.empty());

    serviceWithMock.generateClaimReviewReport(mediator, null);

    verify(claimReviewProcessor)
        .listClaimReviews(eq(mediator), isNull(), isNull(), isNull(), eq(Pageable.unpaged()));
  }

  private static Page<ClaimReviewResponseDto> pageOf(final ClaimReviewResponseDto row) {
    return new PageImpl<>(List.of(row));
  }
}
