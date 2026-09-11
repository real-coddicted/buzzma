package com.coddicted.buzzma.claim.strategy;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClaimCreationContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimCreationContext.class);

  private final DealService dealService;
  private final CampaignService campaignService;
  private final CampaignSlotService campaignSlotService;
  private final CampaignStepResolver campaignStepResolver;
  private final StorageService storageService;
  private final ExtractionService extractionService;
  private final CodeGenerationService codeGenerationService;
  private final ClaimService claimService;

  public ClaimCreationContext(
      final DealService dealService,
      final CampaignService campaignService,
      final CampaignSlotService campaignSlotService,
      final CampaignStepResolver campaignStepResolver,
      final StorageService storageService,
      final ExtractionService extractionService,
      final CodeGenerationService codeGenerationService,
      final ClaimService claimService) {
    this.dealService = dealService;
    this.campaignService = campaignService;
    this.campaignSlotService = campaignSlotService;
    this.campaignStepResolver = campaignStepResolver;
    this.storageService = storageService;
    this.extractionService = extractionService;
    this.codeGenerationService = codeGenerationService;
    this.claimService = claimService;
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

  public ClaimService claimService() {
    return this.claimService;
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
    final int updated = this.campaignSlotService.decrementSlot(deal.getCampaignSlot().getId());
    if (updated == 0) {
      LOGGER.warn("All slots claimed for deal {}", deal.getId());
      throw new BusinessRuleViolationException("All slots have been claimed for this deal");
    }
  }

  public ClaimScreenshot saveScreenshot(
      final UUID claimId, final String storageKey, final ScreenshotType type, final UUID actorId) {
    return this.claimService.saveScreenshot(
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
