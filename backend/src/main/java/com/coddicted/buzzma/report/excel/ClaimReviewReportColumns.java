package com.coddicted.buzzma.report.excel;

import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.util.Arrays;
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
          new ExcelColumn<>("Platform", dto -> dto.getPlatform().getDisplayName()),
          new ExcelColumn<>("Order ID", ClaimReviewResponseDto::getEcommerceOrderId),
          new ExcelColumn<>("Order Date", ClaimReviewReportColumns::formatOrderDate),
          new ExcelColumn<>("Claim Code", ClaimReviewResponseDto::getClaimCode),
          new ExcelColumn<>("Claim Status", dto -> dto.getClaimStatus().getDisplayName()),
          new ExcelColumn<>("Match Score", ClaimReviewResponseDto::getMatchScore),
          new ExcelColumn<>(
              "Review Status",
              dto -> dto.getClaimReviewStatus().getDisplayName(),
              Arrays.stream(ClaimReviewStatus.values())
                  .map(ClaimReviewStatus::getDisplayName)
                  .toList()),
          new ExcelColumn<>("Amount Approved", dto -> null),
          new ExcelColumn<>("Brand Review", dto -> null, List.of("Approved", "Rejected")),
          new ExcelColumn<>("Remarks", dto -> null));

  private static String formatOrderDate(final ClaimReviewResponseDto dto) {
    return dto.getOrderDate() > 0 ? DateTimeUtils.toLocalDate(dto.getOrderDate()).toString() : null;
  }
}
