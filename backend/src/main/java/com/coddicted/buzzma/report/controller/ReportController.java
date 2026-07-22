package com.coddicted.buzzma.report.controller;

import com.coddicted.buzzma.claim.dto.ClaimReviewFilterRequestDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.report.service.ReportService;
import com.coddicted.buzzma.shared.constants.WellKnownReports;
import com.coddicted.buzzma.shared.security.CurrentUser;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.time.LocalDate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

  private static final MediaType XLSX =
      MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

  private final ReportService reportService;

  public ReportController(final ReportService reportService) {
    this.reportService = reportService;
  }

  @PostMapping("/claim-review/excel")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.MEDIATOR)
  public ResponseEntity<byte[]> claimReviewReport(
      @CurrentUser final BuzzmaUser requester,
      @RequestBody(required = false) final ClaimReviewFilterRequestDto filter) {
    final byte[] report = this.reportService.generateClaimReviewReport(requester, filter);
    final String filename =
        WellKnownReports.CLAIM_REVIEW_FILE_BASE_NAME
            + "_"
            + DateTimeUtils.DATE_FORMAT.format(LocalDate.now())
            + ".xlsx";
    return ResponseEntity.ok()
        .contentType(XLSX)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(filename).build().toString())
        .body(report);
  }
}
