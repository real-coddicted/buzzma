package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.enums.ClaimWorkflowStatus;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimServiceImpl extends BaseCrudService implements ClaimService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimServiceImpl.class);

  private final ClaimRepository claimRepository;
  private final ClaimScreenshotRepository claimScreenshotRepository;
  private final DealService dealService;
  private final StorageService storageService;

  public ClaimServiceImpl(
      final ClaimRepository claimRepository,
      final ClaimScreenshotRepository claimScreenshotRepository,
      final DealService dealService,
      final StorageService storageService) {
    this.claimRepository = claimRepository;
    this.claimScreenshotRepository = claimScreenshotRepository;
    this.dealService = dealService;
    this.storageService = storageService;
  }

  @Override
  @Transactional
  public Claim createClaim(
      final Claim claim,
      final byte[] screenshot,
      final String screenshotFilename,
      final String contentType) {

    final Deal deal = this.dealService.getById(claim.getDealId());

    if (this.claimRepository.existsByOwnerIdAndDealIdAndIsDeletedFalse(
        claim.getOwnerId(), claim.getDealId())) {
      LOGGER.warn(
          "Owner {} already has a claim for deal {}", claim.getOwnerId(), claim.getDealId());
      throw new BusinessRuleViolationException("You have already claimed this deal");
    }

    final String screenshotKey =
        this.storageService.store("claims", screenshotFilename, contentType, screenshot);

    final Claim saved =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimWorkflowStatus.ORDERED)
                .isDeleted(false)
                .createdBy(claim.getOwnerId())
                .updatedBy(claim.getOwnerId())
                .build());

    saveScreenshot(
        saved.getId(), screenshotKey, ScreenshotType.SCREENSHOT_TYPE_ORDER, saved.getOwnerId());

    return saved;
  }

  @Override
  @Transactional
  public Claim submitReview(
      final UUID claimId,
      final UUID ownerId,
      final String reviewUrl,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Claim claim = loadAndVerifyOwnership(claimId, ownerId);

    if (claim.getStatus() != ClaimWorkflowStatus.ORDERED) {
      LOGGER.warn(
          "Claim {} in status {} cannot transition to PROOF_SUBMITTED", claimId, claim.getStatus());
      throw new BusinessRuleViolationException(
          "Review can only be submitted when claim is in ORDERED status");
    }

    final String screenshotKey =
        this.storageService.store("claims", filename, contentType, screenshot);

    final Claim updated =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimWorkflowStatus.PROOF_SUBMITTED)
                .reviewUrl(reviewUrl)
                .updatedBy(ownerId)
                .build());

    saveScreenshot(claimId, screenshotKey, ScreenshotType.SCREENSHOT_TYPE_REVIEW, ownerId);

    return updated;
  }

  @Override
  @Transactional
  public Claim submitReturn(
      final UUID claimId,
      final UUID ownerId,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Claim claim = loadAndVerifyOwnership(claimId, ownerId);

    if (claim.getStatus() != ClaimWorkflowStatus.PROOF_SUBMITTED) {
      LOGGER.warn(
          "Claim {} in status {} cannot transition to UNDER_REVIEW", claimId, claim.getStatus());
      throw new BusinessRuleViolationException(
          "Return screenshot can only be submitted when claim is in PROOF_SUBMITTED status");
    }

    final String screenshotKey =
        this.storageService.store("claims", filename, contentType, screenshot);

    final Claim updated =
        this.claimRepository.save(
            claim.toBuilder().status(ClaimWorkflowStatus.UNDER_REVIEW).updatedBy(ownerId).build());

    saveScreenshot(claimId, screenshotKey, ScreenshotType.SCREENSHOT_TYPE_RETURN, ownerId);

    return updated;
  }

  @Override
  @Transactional(readOnly = true)
  public Claim getById(final UUID claimId, final UUID ownerId) {
    return loadAndVerifyOwnership(claimId, ownerId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Claim> listByOwner(final UUID ownerId) {
    return this.claimRepository.findByOwnerIdAndIsDeletedFalse(ownerId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimScreenshot> listScreenshots(final UUID claimId) {
    return this.claimScreenshotRepository.findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(
        claimId);
  }

  private void saveScreenshot(
      final UUID claimId, final String key, final ScreenshotType type, final UUID actorId) {
    this.claimScreenshotRepository.save(
        ClaimScreenshot.builder()
            .claimId(claimId)
            .key(key)
            .type(type)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .isDeleted(false)
            .createdBy(actorId)
            .updatedBy(actorId)
            .build());
  }

  private Claim loadAndVerifyOwnership(final UUID claimId, final UUID ownerId) {
    final Claim claim =
        this.claimRepository
            .findByIdAndIsDeletedFalse(claimId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Claim not found: {}", claimId);
                  return new NotFoundException("Claim not found: " + claimId);
                });
    if (!claim.getOwnerId().equals(ownerId)) {
      LOGGER.warn(
          "User {} attempted to access claim {} owned by {}", ownerId, claimId, claim.getOwnerId());
      throw new ForbiddenException("Access denied");
    }
    return claim;
  }
}
