package com.coddicted.buzzma.claim.strategy;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The collaborators and shared mechanics every {@link ClaimCreationStrategy} needs, bundled into
 * one injectable bean since a plain interface can't hold instance state. {@code
 * loadActiveCampaign}/{@code decrementSlotOrThrow}/{@code saveScreenshot} intentionally duplicate
 * the equivalent private methods in {@code ClaimServiceImpl} for now — {@code createClaim}/{@code
 * createAppReviewClaim} haven't been migrated onto the strategy pattern yet, so removing those
 * would break the still-live methods. The duplication is meant to be temporary: once a future
 * change migrates those methods onto strategies, {@code ClaimServiceImpl} should delegate to this
 * context instead of keeping its own copies.
 */
@Component
public class ClaimCreationContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimCreationContext.class);

  private final DealService dealService;
  private final CampaignService campaignService;
  private final CampaignSlotRepository campaignSlotRepository;
  private final CampaignStepResolver campaignStepResolver;
  private final StorageService storageService;
  private final ExtractionService extractionService;
  private final CodeGenerationService codeGenerationService;
  private final ClaimRepository claimRepository;
  private final ClaimScreenshotRepository claimScreenshotRepository;

  public ClaimCreationContext(
      final DealService dealService,
      final CampaignService campaignService,
      final CampaignSlotRepository campaignSlotRepository,
      final CampaignStepResolver campaignStepResolver,
      final StorageService storageService,
      final ExtractionService extractionService,
      final CodeGenerationService codeGenerationService,
      final ClaimRepository claimRepository,
      final ClaimScreenshotRepository claimScreenshotRepository) {
    this.dealService = dealService;
    this.campaignService = campaignService;
    this.campaignSlotRepository = campaignSlotRepository;
    this.campaignStepResolver = campaignStepResolver;
    this.storageService = storageService;
    this.extractionService = extractionService;
    this.codeGenerationService = codeGenerationService;
    this.claimRepository = claimRepository;
    this.claimScreenshotRepository = claimScreenshotRepository;
  }

  public DealService dealService() {
    return this.dealService;
  }

  public CampaignStepResolver campaignStepResolver() {
    return this.campaignStepResolver;
  }

  public StorageService storageService() {
    return this.storageService;
  }

  public ExtractionService extractionService() {
    return this.extractionService;
  }

  public CodeGenerationService codeGenerationService() {
    return this.codeGenerationService;
  }

  public ClaimRepository claimRepository() {
    return this.claimRepository;
  }

  public Campaign loadActiveCampaign(final Claim claim) {
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

  public void decrementSlotOrThrow(final Deal deal) {
    final int updated =
        this.campaignSlotRepository.decrementSlotsAvailableIfPositive(
            deal.getCampaignSlot().getId());
    if (updated == 0) {
      LOGGER.warn("All slots claimed for deal {}", deal.getId());
      throw new BusinessRuleViolationException("All slots have been claimed for this deal");
    }
  }

  public ClaimScreenshot saveScreenshot(
      final UUID claimId, final String storageKey, final ScreenshotType type, final UUID actorId) {
    return this.claimScreenshotRepository.save(
        ClaimScreenshot.builder()
            .claimId(claimId)
            .storageKey(storageKey)
            .type(type)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .isDeleted(false)
            .createdBy(actorId)
            .updatedBy(actorId)
            .build());
  }
}
