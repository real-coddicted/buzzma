package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import com.coddicted.buzzma.claim.exception.RowValidationException;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRowRepository;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetRowService;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ClaimReviewWorksheetRowServiceImpl implements ClaimReviewWorksheetRowService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ClaimReviewWorksheetRowServiceImpl.class);

  private static final Set<ClaimStatus> TERMINAL_STATUSES =
      Set.of(ClaimStatus.APPROVED, ClaimStatus.REJECTED, ClaimStatus.COMPLETED, ClaimStatus.FAILED);

  @PersistenceContext private EntityManager entityManager;

  private final ClaimReviewWorksheetRowRepository rowRepository;
  private final ClaimReviewWorksheetService worksheetService;
  private final ClaimService claimService;
  private final ClaimReviewService claimReviewService;
  private final CampaignService campaignService;
  private final CampaignShareService campaignShareService;

  public ClaimReviewWorksheetRowServiceImpl(
      final ClaimReviewWorksheetRowRepository rowRepository,
      final ClaimReviewWorksheetService worksheetService,
      final ClaimService claimService,
      final ClaimReviewService claimReviewService,
      final CampaignService campaignService,
      final CampaignShareService campaignShareService) {
    this.rowRepository = rowRepository;
    this.worksheetService = worksheetService;
    this.claimService = claimService;
    this.claimReviewService = claimReviewService;
    this.campaignService = campaignService;
    this.campaignShareService = campaignShareService;
  }

  @Override
  @Transactional
  @SuppressWarnings("unchecked")
  public List<UUID> claimBatchForProcessing(final int batchSize, final int maxRetries) {
    List<Object> rawIds =
        entityManager
            .createNativeQuery(
                """
            UPDATE claim_review_worksheet_rows
            SET processing_status  = 'IN_PROGRESS',
                last_attempted_at  = NOW()
            WHERE id IN (
                SELECT id FROM claim_review_worksheet_rows
                WHERE processing_status = 'PENDING'
                  AND retry_count < :maxRetries
                ORDER BY last_attempted_at NULLS FIRST
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            )
            RETURNING id
            """)
            .setParameter("maxRetries", maxRetries)
            .setParameter("batchSize", batchSize)
            .getResultList();

    return rawIds.stream()
        .map(id -> id instanceof UUID ? (UUID) id : UUID.fromString(id.toString()))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void processRow(final ClaimReviewWorksheetRow row) {
    LOGGER.info(
        "ClaimReviewWorksheetRowService: processing row id={} worksheetId={}",
        row.getId(),
        row.getWorksheetId());
    try {
      // Group 1: pure field checks — no DB calls
      final ReviewerDecision decision = validateBrandReview(row);
      validateRemarksForRejection(row, decision);

      // Group 2: claim validity — 1 DB call
      final Claim claim = loadAndValidateClaim(row);
      validateClaimIsReviewable(claim);

      // Group 3: authority, amount, bounds, submit — 2-3 DB calls
      final UUID reviewerId = loadReviewerId(row);
      final UserRole reviewerRole = validateAuthority(reviewerId, claim);
      final BigInteger amountPaise = validateAmountForApproval(row, decision, claim);
      validateAmountBounds(amountPaise, claim);

      claimReviewService.submitClaimReview(
          claim.getId(), reviewerId, reviewerRole, decision, row.getRemarks(), amountPaise);

      rowRepository.markSuccess(row.getId());
      syncWorksheetStatus(row.getWorksheetId());

    } catch (RowValidationException e) {
      markRowError(row.getId(), row.getWorksheetId(), e.getMessage());
    }
  }

  @Override
  @Transactional
  public void resetForRetry(final UUID rowId) {
    rowRepository.resetForRetry(rowId);
  }

  @Override
  @Transactional
  public void markFailed(final UUID rowId, final String errorRemarks) {
    rowRepository.markFailed(rowId, errorRemarks);
    ClaimReviewWorksheetRow row =
        rowRepository
            .findById(rowId)
            .orElseThrow(() -> new IllegalStateException("Row not found: " + rowId));
    syncWorksheetStatus(row.getWorksheetId());
  }

  @Override
  public Page<ClaimReviewWorksheetRow> listRows(
      final UUID worksheetId,
      final UUID uploadedBy,
      final WorksheetRowStatus status,
      final Pageable pageable) {
    worksheetService.getUploadedBy(worksheetId);
    return rowRepository.findByWorksheetIdAndUploadByAndStatuses(
        worksheetId, uploadedBy, status == null ? null : List.of(status), pageable);
  }

  // -- Validation: Group 1 (pure field checks, no DB) --

  private ReviewerDecision validateBrandReview(final ClaimReviewWorksheetRow row) {
    if (!StringUtils.hasText(row.getBrandReview())) {
      throw new RowValidationException(
          "Invalid brand review, only APPROVED or REJECTED is allowed");
    }
    try {
      ReviewerDecision decision =
          ReviewerDecision.valueOf(row.getBrandReview().trim().toUpperCase());
      if (decision == ReviewerDecision.VERIFIED) {
        throw new RowValidationException(
            "Invalid brand review, only APPROVED or REJECTED is allowed");
      }
      return decision;
    } catch (IllegalArgumentException e) {
      throw new RowValidationException(
          "Invalid brand review, only APPROVED or REJECTED is allowed");
    }
  }

  private BigInteger validateAmountForApproval(
      final ClaimReviewWorksheetRow row, final ReviewerDecision decision, final Claim claim) {
    if (decision != ReviewerDecision.APPROVED) {
      return null;
    }
    if (!StringUtils.hasText(row.getAmountApproved())) {
      throw new RowValidationException("Amount approved is required for approved claims");
    }
    final BigDecimal rupees;
    try {
      rupees = new BigDecimal(row.getAmountApproved().trim());
    } catch (NumberFormatException e) {
      throw new RowValidationException("Invalid amount format: " + row.getAmountApproved().trim());
    }
    final BigInteger paise =
        rupees.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toBigInteger();
    // App-review claims may reimburse nothing (a free app), so 0 is valid there; every other
    // campaign type reimburses an order and must be strictly positive.
    final boolean appReview =
        campaignService.getById(claim.getCampaignId()).getType()
            == CampaignType.CAMPAIGN_TYPE_APP_REVIEW;
    if (appReview ? paise.compareTo(BigInteger.ZERO) < 0 : paise.compareTo(BigInteger.ZERO) <= 0) {
      throw new RowValidationException(
          appReview
              ? "Approved amount cannot be negative"
              : "Approved amount must be greater than zero");
    }
    return paise;
  }

  private void validateRemarksForRejection(
      final ClaimReviewWorksheetRow row, final ReviewerDecision decision) {
    if (decision == ReviewerDecision.REJECTED && !StringUtils.hasText(row.getRemarks())) {
      throw new RowValidationException("Rejection reason required for rejected claims");
    }
  }

  // -- Validation: Group 2 (claim DB checks) --

  private Claim loadAndValidateClaim(final ClaimReviewWorksheetRow row) {
    final Claim claim;
    try {
      claim = claimService.getByCode(row.getClaimCode());
    } catch (NotFoundException e) {
      throw new RowValidationException("Invalid claim-code, order-id combination");
    }
    if (!row.getOrderId().equals(claim.getEcommerceOrderId())) {
      throw new RowValidationException("Invalid claim-code, order-id combination");
    }
    return claim;
  }

  private void validateClaimIsReviewable(final Claim claim) {
    if (TERMINAL_STATUSES.contains(claim.getStatus())) {
      throw new RowValidationException("Claim already processed earlier");
    }
    if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
      throw new RowValidationException("Claim is not in a reviewable state");
    }
  }

  // -- Validation: Group 3 (authority and bounds) --

  private UUID loadReviewerId(final ClaimReviewWorksheetRow row) {
    return worksheetService.getUploadedBy(row.getWorksheetId());
  }

  private UserRole validateAuthority(final UUID reviewerId, final Claim claim) {
    final var campaign = campaignService.getById(claim.getCampaignId());
    if (campaign.getOwnerId().equals(reviewerId)) {
      return UserRole.ROLE_AGENCY;
    }
    final Optional<CampaignShare> share =
        campaignShareService.findByCampaignId(claim.getCampaignId());
    if (share.isPresent() && share.get().getToUserId().equals(reviewerId)) {
      return UserRole.ROLE_BRAND;
    }
    throw new RowValidationException("User does not have authority to review this claim");
  }

  private static void validateAmountBounds(final BigInteger amountPaise, final Claim claim) {
    if (amountPaise == null || claim.getAmountPaise() == null) {
      return;
    }
    if (amountPaise.compareTo(claim.getAmountPaise()) > 0) {
      throw new RowValidationException("Approved amount cannot exceed claimed amount");
    }
  }

  // -- Helpers --

  private void markRowError(final UUID rowId, final UUID worksheetId, final String message) {
    rowRepository.markFailed(rowId, message);
    syncWorksheetStatus(worksheetId);
  }

  private void syncWorksheetStatus(final UUID worksheetId) {
    if (!rowRepository.allRowsTerminalForWorksheet(worksheetId)) {
      return;
    }
    WorksheetRowStatus finalStatus =
        rowRepository.anyRowFailedForWorksheet(worksheetId)
            ? WorksheetRowStatus.ERROR
            : WorksheetRowStatus.SUCCESS;
    worksheetService.updateStatus(worksheetId, finalStatus);
    LOGGER.info("ClaimReviewWorksheetRowService: worksheet {} marked {}", worksheetId, finalStatus);
  }
}
