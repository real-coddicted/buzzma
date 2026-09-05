package com.coddicted.buzzma.report.excel;

import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Column layout for the claim review Excel export. */
public final class ClaimReviewReportColumns {

  private ClaimReviewReportColumns() {}

  public static final List<ExcelColumn<ClaimReviewResponseDto>> COLUMNS =
      List.of(
          new ExcelColumn<>("Campaign", ClaimReviewResponseDto::getCampaignName),
          new ExcelColumn<>("Campaign Code", ClaimReviewResponseDto::getCampaignCode),
          new ExcelColumn<>("Brand", ClaimReviewResponseDto::getBrandName),
          new ExcelColumn<>("Mediator", ClaimReviewResponseDto::getDealOwnerName),
          new ExcelColumn<>("Buyer", ClaimReviewResponseDto::getBuyerName),
          new ExcelColumn<>("Profile Name", ClaimReviewResponseDto::getAccountName),
          new ExcelColumn<>("Platform", dto -> dto.getPlatform().getDisplayName()),
          new ExcelColumn<>("Order ID", ClaimReviewResponseDto::getEcommerceOrderId),
          new ExcelColumn<>("Order Date", ClaimReviewReportColumns::formatOrderDate),
          new ExcelColumn<>("Order Amount", ClaimReviewReportColumns::formatOrderAmount),
          new ExcelColumn<>("Claim Code", ClaimReviewResponseDto::getClaimCode),
          new ExcelColumn<>("Claim Status", dto -> dto.getClaimStatus().getDisplayName()),
          new ExcelColumn<>("Match Score", ClaimReviewResponseDto::getMatchScore),
          new ExcelColumn<>("Amount Approved", dto -> null),
          new ExcelColumn<>("Brand Review", dto -> null, List.of("Approved", "Rejected")),
          new ExcelColumn<>("Remarks", dto -> null));

  private static final String AMOUNT_APPROVED_HEADER = "Amount Approved";

  /**
   * Brands do not review approved amounts, so the blank "Amount Approved" fill-in column is dropped
   * from their export. Every other role keeps the full layout: the agency worksheet import reads
   * cells back by fixed position, so {@link #COLUMNS} must not be reordered.
   */
  public static List<ExcelColumn<ClaimReviewResponseDto>> columnsFor(final UserRole role) {
    if (role != UserRole.ROLE_BRAND) {
      return COLUMNS;
    }
    return COLUMNS.stream()
        .filter(column -> !AMOUNT_APPROVED_HEADER.equals(column.header()))
        .toList();
  }

  private static String formatOrderDate(final ClaimReviewResponseDto dto) {
    return dto.getOrderDate() > 0 ? DateTimeUtils.toLocalDate(dto.getOrderDate()).toString() : null;
  }

  private static BigDecimal formatOrderAmount(final ClaimReviewResponseDto dto) {
    return dto.getAmountPaise() != null
        ? new BigDecimal(dto.getAmountPaise())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        : null;
  }
}
