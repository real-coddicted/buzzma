package com.coddicted.buzzma.claim.controller;

import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetDownloadDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetResponseDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetRowResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import com.coddicted.buzzma.claim.mapper.ClaimReviewWorksheetMapper;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetRowService;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/claim-review")
public class ClaimReviewController {

  private static final MediaType XLSX =
      MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

  private final ClaimReviewWorksheetService worksheetService;
  private final ClaimReviewWorksheetRowService worksheetRowService;
  private final ClaimReviewWorksheetMapper worksheetMapper;

  public ClaimReviewController(
      final ClaimReviewWorksheetService worksheetService,
      final ClaimReviewWorksheetRowService worksheetRowService,
      final ClaimReviewWorksheetMapper worksheetMapper) {
    this.worksheetService = worksheetService;
    this.worksheetRowService = worksheetRowService;
    this.worksheetMapper = worksheetMapper;
  }

  @GetMapping("/worksheets")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public ResponseEntity<List<ClaimReviewWorksheetResponseDto>> listWorkbooks(
      @CurrentUser final BuzzmaUser currentUser) {
    return ResponseEntity.ok(worksheetService.listWorkbooks(currentUser));
  }

  @PostMapping("/worksheets")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public ResponseEntity<ClaimReviewWorksheetResponseDto> uploadWorksheet(
      @CurrentUser final BuzzmaUser currentUser, @RequestParam("file") final MultipartFile file) {
    final ClaimReviewWorksheet worksheet = worksheetService.uploadWorksheet(currentUser, file);
    return ResponseEntity.status(HttpStatus.CREATED).body(worksheetMapper.toResponse(worksheet));
  }

  @GetMapping("/worksheets/{id}")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public ResponseEntity<byte[]> downloadWorksheet(@PathVariable final UUID id) {
    final ClaimReviewWorksheetDownloadDto download = worksheetService.downloadWorksheet(id);
    final String filename =
        download.originalFilename() != null ? download.originalFilename() : "worksheet.xlsx";
    return ResponseEntity.ok()
        .contentType(XLSX)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(filename).build().toString())
        .body(download.bytes());
  }

  @GetMapping("/worksheets/{id}/rows")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public ResponseEntity<Page<ClaimReviewWorksheetRowResponseDto>> listWorksheetRows(
      @CurrentUser final BuzzmaUser currentUser,
      @PathVariable final UUID id,
      @RequestParam(required = false) final WorksheetRowStatus status,
      final Pageable pageable) {
    final Page<ClaimReviewWorksheetRow> rows =
        worksheetRowService.listRows(id, currentUser.getId(), status, pageable);
    return ResponseEntity.ok(rows.map(worksheetMapper::toRowResponse));
  }
}
