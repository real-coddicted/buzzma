package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotScorerUtils;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimServiceImpl extends BaseCrudService implements ClaimService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimServiceImpl.class);

  private final ClaimRepository claimRepository;
  private final ClaimScreenshotRepository claimScreenshotRepository;
  private final CampaignService campaignService;
  private final CampaignShareService campaignShareService;
  private final DealService dealService;
  private final CampaignSlotRepository campaignSlotRepository;
  private final CampaignStepResolver campaignStepResolver;
  private final StorageService storageService;
  private final ExtractionService extractionService;
  private final CodeGenerationService codeGenerationService;

  public ClaimServiceImpl(
      final ClaimRepository claimRepository,
      final ClaimScreenshotRepository claimScreenshotRepository,
      final CampaignService campaignService,
      final CampaignShareService campaignShareService,
      final DealService dealService,
      final CampaignSlotRepository campaignSlotRepository,
      final CampaignStepResolver campaignStepResolver,
      final StorageService storageService,
      final ExtractionService extractionService,
      final CodeGenerationService codeGenerationService) {
    this.claimRepository = claimRepository;
    this.claimScreenshotRepository = claimScreenshotRepository;
    this.campaignService = campaignService;
    this.campaignShareService = campaignShareService;
    this.dealService = dealService;
    this.campaignSlotRepository = campaignSlotRepository;
    this.campaignStepResolver = campaignStepResolver;
    this.storageService = storageService;
    this.extractionService = extractionService;
    this.codeGenerationService = codeGenerationService;
  }

  @Override
  @Transactional
  public Claim createClaim(
      final Claim claim,
      final byte[] screenshot,
      final String screenshotFilename,
      final String contentType,
      final Map<String, ScoredValue> extractedDetails,
      final Integer overallScore) {

    if (this.claimRepository.existsByEcommerceOrderIdAndPlatformAndIsDeletedFalse(
        claim.getEcommerceOrderId(), claim.getPlatform())) {
      LOGGER.warn(
          "Order {} on platform {} has already been claimed",
          claim.getEcommerceOrderId(),
          claim.getPlatform());
      throw new BusinessRuleViolationException("Claim with this Order ID has already been placed");
    }

    final Deal deal = this.dealService.getById(claim.getDealId());

    final Campaign campaign = loadActiveCampaign(claim);

    validateExchangeProduct(campaign, claim);

    final int updated =
        this.campaignSlotRepository.decrementSlotsAvailableIfPositive(
            deal.getCampaignSlot().getId());
    if (updated == 0) {
      LOGGER.warn("All slots claimed for deal {}", claim.getDealId());
      throw new BusinessRuleViolationException("All slots have been claimed for this deal");
    }

    final String screenshotKey =
        this.storageService.store("claims", screenshotFilename, contentType, screenshot);

    final ExtractedScoredResult extractedScoredResult =
        ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
            claim, extractedDetails, overallScore);

    final String code =
        this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.CLAIM);

    final boolean campaignHasSellerName = campaign.getSellerName() != null;

    final Claim saved =
        this.claimRepository.save(
            claim.toBuilder()
                .code(code)
                .status(ClaimStatus.ORDERED)
                .score(extractedScoredResult.overallScore())
                .isDeleted(false)
                .createdBy(claim.getOwnerId())
                .updatedBy(claim.getOwnerId())
                .currentStep(CampaignStepType.ORDER)
                .sellerName(campaignHasSellerName ? claim.getSellerName() : null)
                .build());

    saveScreenshot(
        saved.getId(),
        screenshotKey,
        ScreenshotType.SCREENSHOT_TYPE_ORDER,
        saved.getOwnerId(),
        extractedScoredResult.extractedResult(),
        extractedScoredResult.overallScore());

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
    final List<CampaignStepType> steps = this.campaignStepResolver.resolve(deal.getCampaign());
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
    final List<CampaignStepType> steps = this.campaignStepResolver.resolve(deal.getCampaign());
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
    final List<CampaignStepType> steps = this.campaignStepResolver.resolve(deal.getCampaign());
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
  @Transactional
  public ClaimWithDeal submitDelivery(
      final UUID claimId,
      final UUID ownerId,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Claim claim = loadAndVerifyOwnership(claimId, ownerId);
    final Deal deal = this.dealService.getById(claim.getDealId());
    final List<CampaignStepType> steps = this.campaignStepResolver.resolve(deal.getCampaign());
    validatePrecedingStep(steps, CampaignStepType.DELIVERY, claim.getCurrentStep());

    final String screenshotKey =
        this.storageService.store("claims", filename, contentType, screenshot);

    final Claim updated =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimStatus.DELIVERY_PROOF_SUBMITTED)
                .currentStep(CampaignStepType.DELIVERY)
                .updatedBy(ownerId)
                .build());

    final ClaimScreenshot deliveryScreenshot =
        saveScreenshot(claimId, screenshotKey, ScreenshotType.SCREENSHOT_TYPE_DELIVERY, ownerId);
    this.extractionService.submitJob(deliveryScreenshot.getId(), ownerId);

    return new ClaimWithDeal(updated, deal);
  }

  @Override
  @Transactional
  public ClaimWithDeal submitSellerFeedback(
      final UUID claimId,
      final UUID ownerId,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Claim claim = loadAndVerifyOwnership(claimId, ownerId);
    final Deal deal = this.dealService.getById(claim.getDealId());
    final List<CampaignStepType> steps = this.campaignStepResolver.resolve(deal.getCampaign());
    validatePrecedingStep(steps, CampaignStepType.SELLER_FEEDBACK, claim.getCurrentStep());

    final String screenshotKey =
        this.storageService.store("claims", filename, contentType, screenshot);

    final Claim updated =
        this.claimRepository.save(
            claim.toBuilder()
                .status(ClaimStatus.SELLER_FEEDBACK_SUBMITTED)
                .currentStep(CampaignStepType.SELLER_FEEDBACK)
                .updatedBy(ownerId)
                .build());

    final ClaimScreenshot sellerFeedbackScreenshot =
        saveScreenshot(
            claimId, screenshotKey, ScreenshotType.SCREENSHOT_TYPE_SELLER_FEEDBACK, ownerId);
    this.extractionService.submitJob(sellerFeedbackScreenshot.getId(), ownerId);

    return new ClaimWithDeal(updated, deal);
  }

  @Override
  @Transactional(readOnly = true)
  public Claim getById(final UUID claimId, final UUID ownerId) {
    return loadAndVerifyOwnership(claimId, ownerId);
  }

  @Override
  @Transactional(readOnly = true)
  public Claim getByCode(final String code) {
    return this.claimRepository
        .findByCodeAndIsDeletedFalse(code)
        .orElseThrow(() -> new NotFoundException("Claim not found: " + code));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Claim> listByOwner(final UUID ownerId, final int page, final int size) {
    final Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return this.claimRepository.findByOwnerIdAndIsDeletedFalse(ownerId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<UUID, Claim> findAllByIdAsMap(final Collection<UUID> claimIds) {
    return this.claimRepository.findAllById(claimIds).stream()
        .collect(Collectors.toMap(Claim::getId, Function.identity()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimScreenshot> listScreenshots(final UUID claimId) {
    return this.claimScreenshotRepository.findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(
        claimId);
  }

  @Override
  @Transactional
  public ClaimWithDeal updateScreenshot(
      final UUID claimId,
      final UUID requesterId,
      final UUID screenshotId,
      final ScreenshotType screenshotType,
      final byte[] screenshot,
      final String filename,
      final String contentType,
      final OrderUpdateFields orderFields,
      final String reviewUrl) {

    final Claim claim = loadAndVerifyOwnership(claimId, requesterId);

    final ClaimScreenshot existing =
        this.claimScreenshotRepository
            .findById(screenshotId)
            .orElseThrow(() -> new NotFoundException("Screenshot not found: " + screenshotId));

    if (!claimId.equals(existing.getClaimId())) {
      throw new NotFoundException("Screenshot not found: " + screenshotId);
    }
    if (screenshotType != existing.getType()) {
      throw new BusinessRuleViolationException(
          "Screenshot type mismatch: expected " + existing.getType());
    }

    this.storageService.delete(existing.getStorageKey());
    final String newKey = this.storageService.store("claims", filename, contentType, screenshot);

    final ClaimScreenshot updated =
        this.claimScreenshotRepository.save(
            existing.toBuilder()
                .storageKey(newKey)
                .verificationStatus(
                    ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
                .extractedDetails(null)
                .score(null)
                .updatedAt(Instant.now())
                .updatedBy(requesterId)
                .build());

    this.extractionService.submitJob(updated.getId(), requesterId);

    final Claim finalClaim =
        updateClaim(requesterId, screenshotType, orderFields, reviewUrl, claim);

    return new ClaimWithDeal(finalClaim, this.dealService.getById(finalClaim.getDealId()));
  }

  private Claim updateClaim(
      final UUID requesterId,
      final ScreenshotType screenshotType,
      final OrderUpdateFields orderFields,
      final String reviewUrl,
      Claim claim) {

    Claim.ClaimBuilder b = claim.toBuilder();
    if (screenshotType == ScreenshotType.SCREENSHOT_TYPE_REVIEW && reviewUrl != null) {
      b = b.reviewUrl(reviewUrl).updatedBy(requesterId);
    }
    if (screenshotType == ScreenshotType.SCREENSHOT_TYPE_ORDER && orderFields != null) {
      b = claim.toBuilder().updatedBy(requesterId);
      if (orderFields.platform() != null) {
        b.platform(orderFields.platform());
      }
      if (orderFields.ecommerceOrderId() != null) {
        b.ecommerceOrderId(orderFields.ecommerceOrderId());
      }
      if (orderFields.amountPaise() != null) {
        b.amountPaise(orderFields.amountPaise());
      }
      if (orderFields.productName() != null) {
        b.productName(orderFields.productName());
      }
      if (orderFields.sellerName() != null
          && this.campaignService.getById(claim.getCampaignId()).getSellerName() != null) {
        b.sellerName(orderFields.sellerName());
      }
      if (orderFields.orderDate() != null) {
        b.orderDate(orderFields.orderDate());
      }
      if (orderFields.accountName() != null) {
        b.accountName(orderFields.accountName());
      }
      if (orderFields.exchangeProduct() != null) {
        b.exchangeProduct(orderFields.exchangeProduct());
      }
    }
    claim = verifyAndUpdateClaimStatus(b.build(), requesterId);
    claim = this.claimRepository.save(claim);
    return claim;
  }

  @Override
  @Transactional
  public void updateClaimScore(final UUID claimId) {
    this.claimRepository.updateScoreFromScreenshots(claimId);
  }

  @Override
  @Transactional
  public void markAccountingCompleted(final UUID claimId) {
    this.claimRepository.markAccountingCompleted(claimId);
  }

  @Override
  @Transactional
  public Claim save(final Claim claim) {
    return this.claimRepository.save(claim);
  }

  @Override
  @Transactional(readOnly = true)
  public ClaimScreenshot getScreenshotById(final UUID screenshotId) {
    return this.claimScreenshotRepository
        .findById(screenshotId)
        .orElseThrow(() -> new NotFoundException("Screenshot not found: " + screenshotId));
  }

  @Override
  @Transactional
  public ClaimScreenshot saveScreenshot(final ClaimScreenshot screenshot) {
    return this.claimScreenshotRepository.save(screenshot);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClaimReviewModel> findClaimsToReviewForMediator(
      final UUID mediatorId,
      final Collection<UUID> campaignIds,
      final Collection<ClaimStatus> claimStatuses,
      final Collection<String> brands,
      final Collection<Platform> platforms,
      final Pageable pageable) {
    return this.claimRepository.findClaimsToReviewForMediator(
        mediatorId, campaignIds, claimStatuses, brands, platforms, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClaimReviewModel> findClaimsToReviewForCampaigns(
      final Collection<UUID> campaignIds,
      final Collection<UUID> mediatorIds,
      final Collection<ClaimStatus> claimStatuses,
      final Collection<String> brands,
      final Collection<Platform> platforms,
      final Pageable pageable) {
    return this.claimRepository.findClaimsToReviewForCampaigns(
        campaignIds, mediatorIds, claimStatuses, brands, platforms, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimReviewModel> findClaimReviewModels(final Collection<UUID> claimIds) {
    return this.claimRepository.findClaimReviewModelsByIds(claimIds);
  }

  private Claim verifyAndUpdateClaimStatus(final Claim claim, final UUID requesterId) {
    final List<ClaimScreenshot> screenshots =
        this.claimScreenshotRepository.findByClaimIdAndIsDeletedFalseOrderByCreatedAtAsc(
            claim.getId());

    final boolean hasRejected =
        screenshots.stream()
            .anyMatch(
                s ->
                    s.getVerificationStatus()
                        == ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_REJECTED);
    if (hasRejected) {
      // No need to update claim status (Which should be objected at this point)
      return claim;
    }
    // Otherwise status update is needed depending on already completed steps
    // setting default status values below
    ClaimStatus claimStatus = ClaimStatus.ORDERED;

    final boolean hasReturn =
        screenshots.stream().anyMatch(s -> s.getType() == ScreenshotType.SCREENSHOT_TYPE_RETURN);

    if (hasReturn) {
      // This should be revisited in case of change in end step
      claimStatus = ClaimStatus.UNDER_REVIEW;
    }

    return claim.toBuilder().status(claimStatus).updatedBy(requesterId).build();
  }

  private ClaimScreenshot saveScreenshot(
      final UUID claimId, final String storageKey, final ScreenshotType type, final UUID actorId) {
    return saveScreenshot(claimId, storageKey, type, actorId, null, null);
  }

  private ClaimScreenshot saveScreenshot(
      final UUID claimId,
      final String storageKey,
      final ScreenshotType type,
      final UUID actorId,
      final Map<String, ScoredValue> extractedDetails,
      final Integer score) {
    return this.claimScreenshotRepository.save(
        ClaimScreenshot.builder()
            .claimId(claimId)
            .storageKey(storageKey)
            .type(type)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .extractedDetails(extractedDetails)
            .score(score)
            .isDeleted(false)
            .createdBy(actorId)
            .updatedBy(actorId)
            .build());
  }

  private Campaign loadActiveCampaign(final Claim claim) {
    final Campaign campaign = this.campaignService.getById(claim.getCampaignId());
    if (campaign.getStatus() != CampaignStatus.CAMPAIGN_STATUS_ACTIVE) {
      LOGGER.warn(
          "Claim rejected for deal {}: campaign {} is not active (status {})",
          claim.getDealId(),
          claim.getCampaignId(),
          campaign.getStatus());
      throw new BusinessRuleViolationException(
          "The campaign is not active anymore. Please go back to deals page and refresh once to"
              + " confirm active deals");
    }
    return campaign;
  }

  private void validateExchangeProduct(final Campaign campaign, final Claim claim) {
    final String exchangeProduct = claim.getExchangeProduct();
    if (campaign.getType() == CampaignType.CAMPAIGN_TYPE_EXCHANGE) {
      if (exchangeProduct == null || exchangeProduct.isBlank()) {
        throw new BusinessRuleViolationException(
            "Exchange product is required for exchange campaigns");
      }
      final boolean isConfigured =
          campaign.getExchangeProducts() != null
              && campaign.getExchangeProducts().stream()
                  .anyMatch(product -> exchangeProduct.equals(product.getProductName()));
      if (!isConfigured) {
        throw new BusinessRuleViolationException(
            "Exchange product must be one of the campaign's configured exchange products");
      }
    } else if (exchangeProduct != null && !exchangeProduct.isBlank()) {
      throw new BusinessRuleViolationException(
          "Exchange product is only allowed on exchange campaigns");
    }
  }

  private void validatePrecedingStep(
      final List<CampaignStepType> steps,
      final CampaignStepType targetStep,
      final CampaignStepType currentStep) {
    final int targetIndex = steps.indexOf(targetStep);
    if (targetIndex <= 0) {
      throw new BusinessRuleViolationException("Invalid step configuration for " + targetStep);
    }
    final CampaignStepType expectedStep = steps.get(targetIndex - 1);
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
        || requesterId.equals(this.dealService.getById(claim.getDealId()).getOwnerId())
        || isCampaignSharedWith(claim.getCampaignId(), requesterId)) {
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

  private boolean isCampaignSharedWith(final UUID campaignId, final UUID userId) {
    return this.campaignShareService
        .findByCampaignId(campaignId)
        .map(share -> share.getToUserId().equals(userId))
        .orElse(false);
  }
}
