package com.coddicted.buzzma.report.service.impl;

import com.coddicted.buzzma.claim.dto.ClaimReviewFilterRequestDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.processor.ClaimReviewProcessor;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.report.excel.ClaimReviewReportColumns;
import com.coddicted.buzzma.report.excel.ExcelColumn;
import com.coddicted.buzzma.report.excel.ExcelReportWriter;
import com.coddicted.buzzma.report.service.ReportService;
import com.coddicted.buzzma.shared.constants.WellKnownReports;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {

  private final ClaimReviewProcessor claimReviewProcessor;
  private final ClaimService claimService;
  private final ExcelReportWriter excelReportWriter;

  public ReportServiceImpl(
      final ClaimReviewProcessor claimReviewProcessor,
      final ClaimService claimService,
      final ExcelReportWriter excelReportWriter) {
    this.claimReviewProcessor = claimReviewProcessor;
    this.claimService = claimService;
    this.excelReportWriter = excelReportWriter;
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] generateClaimReviewReport(
      final BuzzmaUser requester, final ClaimReviewFilterRequestDto filter) {
    final List<ClaimReviewResponseDto> rows =
        this.claimReviewProcessor
            .listClaimReviews(
                requester,
                filter != null ? filter.getCampaignIds() : null,
                filter != null ? filter.getMediatorIds() : null,
                filter != null ? filter.getClaimStatuses() : null,
                filter != null ? filter.getBrands() : null,
                filter != null ? filter.getPlatforms() : null,
                Pageable.unpaged())
            .getContent();

    final List<ClaimReviewResponseDto> enrichedRows = enrichWithScreenshotUrls(rows);

    final int maxScreenshots =
        enrichedRows.stream().mapToInt(r -> r.getScreenshotPublicUrls().size()).max().orElse(0);

    final List<ExcelColumn<ClaimReviewResponseDto>> columns =
        new ArrayList<>(ClaimReviewReportColumns.columnsFor(requester.getRole()));
    for (int i = 0; i < maxScreenshots; i++) {
      final int index = i;
      columns.add(
          ExcelColumn.hyperlink(
              "Screenshot " + (i + 1),
              dto ->
                  index < dto.getScreenshotPublicUrls().size()
                      ? dto.getScreenshotPublicUrls().get(index)
                      : null));
    }

    return this.excelReportWriter.write(
        WellKnownReports.CLAIM_REVIEW_SHEET_NAME, columns, enrichedRows);
  }

  private List<ClaimReviewResponseDto> enrichWithScreenshotUrls(
      final List<ClaimReviewResponseDto> rows) {
    if (rows.isEmpty()) {
      return rows;
    }

    final List<UUID> claimIds = rows.stream().map(ClaimReviewResponseDto::getClaimId).toList();

    final Map<UUID, List<String>> urlsByClaimId =
        this.claimService.listScreenshotsByClaimIds(claimIds).stream()
            .filter(s -> s.getPublicUrl() != null)
            .collect(
                Collectors.groupingBy(
                    ClaimScreenshot::getClaimId,
                    Collectors.mapping(ClaimScreenshot::getPublicUrl, Collectors.toList())));

    return rows.stream()
        .map(
            dto ->
                ClaimReviewResponseDto.builder()
                    .id(dto.getId())
                    .campaignId(dto.getCampaignId())
                    .campaignName(dto.getCampaignName())
                    .campaignCode(dto.getCampaignCode())
                    .campaignType(dto.getCampaignType())
                    .dealId(dto.getDealId())
                    .dealOwnerId(dto.getDealOwnerId())
                    .dealOwnerName(dto.getDealOwnerName())
                    .dealOwnerCode(dto.getDealOwnerCode())
                    .buyerName(dto.getBuyerName())
                    .buyerCode(dto.getBuyerCode())
                    .accountName(dto.getAccountName())
                    .claimId(dto.getClaimId())
                    .claimCode(dto.getClaimCode())
                    .claimStatus(dto.getClaimStatus())
                    .ecommerceOrderId(dto.getEcommerceOrderId())
                    .exchangeProduct(dto.getExchangeProduct())
                    .reviewUrl(dto.getReviewUrl())
                    .mediatorVerified(dto.getMediatorVerified())
                    .brandVerified(dto.getBrandVerified())
                    .matchScore(dto.getMatchScore())
                    .amountPaise(dto.getAmountPaise())
                    .amountApprovedPaise(dto.getAmountApprovedPaise())
                    .platform(dto.getPlatform())
                    .orderDate(dto.getOrderDate())
                    .brandName(dto.getBrandName())
                    .createdAt(dto.getCreatedAt())
                    .updatedAt(dto.getUpdatedAt())
                    .screenshotPublicUrls(urlsByClaimId.getOrDefault(dto.getClaimId(), List.of()))
                    .build())
        .toList();
  }
}
