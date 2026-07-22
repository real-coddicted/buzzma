package com.coddicted.buzzma.report.excel;

import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
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
          new ExcelColumn<>("Mediator Code", ClaimReviewResponseDto::getDealOwnerCode),
          new ExcelColumn<>("Buyer", ClaimReviewResponseDto::getBuyerName),
          new ExcelColumn<>("Buyer Code", ClaimReviewResponseDto::getBuyerCode),
          new ExcelColumn<>("Platform", ClaimReviewResponseDto::getPlatform),
          new ExcelColumn<>("Order ID", ClaimReviewResponseDto::getEcommerceOrderId),
          new ExcelColumn<>("Order Date", ClaimReviewReportColumns::formatOrderDate),
          new ExcelColumn<>("Claim Status", ClaimReviewResponseDto::getClaimStatus),
          new ExcelColumn<>("Review Status", ClaimReviewResponseDto::getClaimReviewStatus),
          new ExcelColumn<>("Match Score", ClaimReviewResponseDto::getMatchScore),
          new ExcelColumn<>("Mediator Verified", ClaimReviewResponseDto::getMediatorVerified),
          new ExcelColumn<>("Created At", dto -> DateTimeUtils.formatTimestamp(dto.getCreatedAt())),
          new ExcelColumn<>(
              "Updated At", dto -> DateTimeUtils.formatTimestamp(dto.getUpdatedAt())));

  private static String formatOrderDate(final ClaimReviewResponseDto dto) {
    return dto.getOrderDate() > 0 ? DateTimeUtils.toLocalDate(dto.getOrderDate()).toString() : null;
  }
}
