package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimServiceImpl extends BaseCrudService implements ClaimService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimServiceImpl.class);

  private final ClaimRepository claimRepository;
  private final ClaimScreenshotRepository claimScreenshotRepository;
  private final CampaignService campaignService;
  private final DealService dealService;
  private final CampaignSlotRepository campaignSlotRepository;
  private final CampaignTypeStepService campaignTypeStepService;
  private final StorageService storageService;
  private final ExtractionService extractionService;

  public ClaimServiceImpl(
      final ClaimRepository claimRepository,
      final ClaimScreenshotRepository claimScreenshotRepository,
      final CampaignService campaignService,
      final DealService dealService,
      final CampaignSlotRepository campaignSlotRepository,
      final CampaignTypeStepService campaignTypeStepService,
      final StorageService storageService,
      final ExtractionService extractionService) {
    this.claimRepository = claimRepository;
    this.claimScreenshotRepository = claimScreenshotRepository;
    this.campaignService = campaignService;
    this.dealService = dealService;
    this.campaignSlotRepository = campaignSlotRepository;
    this.campaignTypeStepService = campaignTypeStepService;
    this.storageService = storageService;
    this.extractionService = extractionService;
  }

  @Override
  @Transactional
  public Claim createClaim(
      final Claim claim,
      final byte[] screenshot,
      final String screenshotFilename,
      final String contentType,
      final Map<String, String> extractedDetails) {

    if (this.claimRepository.existsByOwnerIdAndDealIdAndIsDeletedFalse(
        claim.getOwnerId(), claim.getDealId())) {
      LOGGER.warn(
          "Owner {} already has a claim for deal {}", claim.getOwnerId(), claim.getDealId());
      throw new BusinessRuleViolationException("You have already claimed this deal");
    }

    final Deal deal = this.dealService.getById(claim.getDealId());

    final int updated =
        this.campaignSlotRepository.decrementSlotsAvailableIfPositive(
            deal.getCampaignSlot().getId());
    if (updated == 0) {
      LOGGER.warn("All slots claimed for deal {}", claim.getDealId());
      throw new BusinessRuleViolationException("All slots have been claimed for this deal");
    }

    final String screenshotKey =
        this.storageService.store("claims", screenshotFilename, contentType, screenshot);

    final Claim saved =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimStatus.ORDERED)
                .isDeleted(false)
                .createdBy(claim.getOwnerId())
                .updatedBy(claim.getOwnerId())
                .currentStep(CampaignStepType.ORDER)
                .build());

    saveScreenshot(
        saved.getId(),
        screenshotKey,
        ScreenshotType.SCREENSHOT_TYPE_ORDER,
        saved.getOwnerId(),
        extractedDetails);

    return saved;
  }

  @Override
  @Transactional
  public ClaimWithDeal submitReview(
      final UUID claimId,
      final UUID ownerId,
      final String reviewUrl,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Claim claim = loadAndVerifyOwnership(claimId, ownerId);
    final Deal deal = this.dealService.getById(claim.getDealId());
    final List<CampaignTypeStep> steps =
        this.campaignTypeStepService
            .getStepConfig()
            .getOrDefault(deal.getCampaign().getType(), List.of());
    validatePrecedingStep(steps, CampaignStepType.REVIEW, claim.getCurrentStep());

    final String screenshotKey =
        this.storageService.store("claims", filename, contentType, screenshot);

    final Claim updated =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimStatus.REVIEW_SUBMITTED)
                .currentStep(CampaignStepType.REVIEW)
                .reviewUrl(reviewUrl)
                .updatedBy(ownerId)
                .build());

    final ClaimScreenshot reviewScreenshot =
        saveScreenshot(claimId, screenshotKey, ScreenshotType.SCREENSHOT_TYPE_REVIEW, ownerId);
    this.extractionService.submitJob(reviewScreenshot.getId(), ownerId);

    return new ClaimWithDeal(updated, deal);
  }

  @Override
  @Transactional
  public ClaimWithDeal submitRating(
      final UUID claimId,
      final UUID ownerId,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Claim claim = loadAndVerifyOwnership(claimId, ownerId);
    final Deal deal = this.dealService.getById(claim.getDealId());
    final List<CampaignTypeStep> steps =
        this.campaignTypeStepService
            .getStepConfig()
            .getOrDefault(deal.getCampaign().getType(), List.of());
    validatePrecedingStep(steps, CampaignStepType.RATING, claim.getCurrentStep());

    final String screenshotKey =
        this.storageService.store("claims", filename, contentType, screenshot);

    final Claim updated =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimStatus.RATING_SUBMITTED)
                .currentStep(CampaignStepType.RATING)
                .updatedBy(ownerId)
                .build());

    final ClaimScreenshot ratingScreenshot =
        saveScreenshot(claimId, screenshotKey, ScreenshotType.SCREENSHOT_TYPE_RATING, ownerId);
    this.extractionService.submitJob(ratingScreenshot.getId(), ownerId);

    return new ClaimWithDeal(updated, deal);
  }

  @Override
  @Transactional
  public ClaimWithDeal submitReturn(
      final UUID claimId,
      final UUID ownerId,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Claim claim = loadAndVerifyOwnership(claimId, ownerId);
    final Deal deal = this.dealService.getById(claim.getDealId());
    final List<CampaignTypeStep> steps =
        this.campaignTypeStepService
            .getStepConfig()
            .getOrDefault(deal.getCampaign().getType(), List.of());
    validatePrecedingStep(steps, CampaignStepType.RETURN_WINDOW, claim.getCurrentStep());

    final String screenshotKey =
        this.storageService.store("claims", filename, contentType, screenshot);

    final Claim updated =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimStatus.UNDER_REVIEW)
                .currentStep(CampaignStepType.RETURN_WINDOW)
                .updatedBy(ownerId)
                .build());

    final ClaimScreenshot returnScreenshot =
        saveScreenshot(claimId, screenshotKey, ScreenshotType.SCREENSHOT_TYPE_RETURN, ownerId);
    this.extractionService.submitJob(returnScreenshot.getId(), ownerId);

    return new ClaimWithDeal(updated, deal);
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

  @Override
  @Transactional(readOnly = true)
  public Page<Claim> listClaimByCampaignIds(
      final List<UUID> campaignIdList, final Pageable pageable) {
    return this.claimRepository.findByCampaignIdInAndIsDeletedFalse(campaignIdList, pageable);
  }

  private ClaimScreenshot saveScreenshot(
      final UUID claimId, final String storageKey, final ScreenshotType type, final UUID actorId) {
    return saveScreenshot(claimId, storageKey, type, actorId, null);
  }

  private ClaimScreenshot saveScreenshot(
      final UUID claimId,
      final String storageKey,
      final ScreenshotType type,
      final UUID actorId,
      final Map<String, String> extractedDetails) {
    return this.claimScreenshotRepository.save(
        ClaimScreenshot.builder()
            .claimId(claimId)
            .storageKey(storageKey)
            .type(type)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .extractedDetails(extractedDetails)
            .isDeleted(false)
            .createdBy(actorId)
            .updatedBy(actorId)
            .build());
  }

  private void validatePrecedingStep(
      final List<CampaignTypeStep> steps,
      final CampaignStepType targetStep,
      final CampaignStepType currentStep) {
    final List<CampaignTypeStep> sorted =
        steps.stream().sorted(Comparator.comparingInt(CampaignTypeStep::getStepOrder)).toList();
    int targetIndex = -1;
    for (int i = 0; i < sorted.size(); i++) {
      if (sorted.get(i).getId().getStepType() == targetStep) {
        targetIndex = i;
        break;
      }
    }
    if (targetIndex <= 0) {
      throw new BusinessRuleViolationException("Invalid step configuration for " + targetStep);
    }
    final CampaignStepType expectedStep = sorted.get(targetIndex - 1).getId().getStepType();
    if (currentStep != expectedStep) {
      LOGGER.warn(
          "Cannot submit {} — currentStep is {}, expected {}",
          targetStep,
          currentStep,
          expectedStep);
      throw new BusinessRuleViolationException(
          targetStep.getLabel() + " can only be submitted after " + expectedStep.getLabel());
    }
  }

  private Claim loadAndVerifyOwnership(final UUID claimId, final UUID requesterId) {
    final Claim claim =
        this.claimRepository
            .findByIdAndIsDeletedFalse(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found: " + claimId));

    if (requesterId.equals(claim.getOwnerId())
        || requesterId.equals(this.campaignService.getById(claim.getCampaignId()).getOwnerId())
        || requesterId.equals(this.dealService.getById(claim.getDealId()).getOwnerId())) {
      return claim;
    }

    LOGGER.warn(
        "Requester {} is not authorized to access claim {} (owner: {}, campaignId: {}, dealId: {})",
        requesterId,
        claimId,
        claim.getOwnerId(),
        claim.getCampaignId(),
        claim.getDealId());
    throw new NotFoundException("Claim not found: " + claimId);
  }
}
