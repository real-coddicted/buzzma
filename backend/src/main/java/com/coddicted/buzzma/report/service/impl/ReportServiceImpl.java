package com.coddicted.buzzma.report.service.impl;

import com.coddicted.buzzma.claim.dto.ClaimReviewFilterRequestDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.processor.ClaimReviewProcessor;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.report.excel.ClaimReviewReportColumns;
import com.coddicted.buzzma.report.excel.ExcelReportWriter;
import com.coddicted.buzzma.report.service.ReportService;
import com.coddicted.buzzma.shared.constants.WellKnownReports;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {

  private final ClaimReviewProcessor claimReviewProcessor;
  private final ExcelReportWriter excelReportWriter;

  public ReportServiceImpl(
      final ClaimReviewProcessor claimReviewProcessor, final ExcelReportWriter excelReportWriter) {
    this.claimReviewProcessor = claimReviewProcessor;
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
                Pageable.unpaged())
            .getContent();
    return this.excelReportWriter.write(
        WellKnownReports.CLAIM_REVIEW_SHEET_NAME, ClaimReviewReportColumns.COLUMNS, rows);
  }
}
