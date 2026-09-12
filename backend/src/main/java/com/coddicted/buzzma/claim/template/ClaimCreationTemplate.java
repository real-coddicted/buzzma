package com.coddicted.buzzma.claim.template;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the template to create claims based on the promotion category.
 *
 * @author payam
 */
public abstract class ClaimCreationTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimCreationTemplate.class);

  private final DealService dealService;
  private final CampaignService campaignService;
  private final CampaignSlotService campaignSlotService;
  private final CampaignStepResolver campaignStepResolver;
  private final StorageService storageService;
  private final ExtractionService extractionService;
  private final CodeGenerationService codeGenerationService;
  private final ClaimService claimService;

  protected ClaimCreationTemplate(
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

  public abstract PromotionCategory category();

  protected abstract Optional<ScreenshotType> screenshotTypeFor(CampaignStepType firstStep);

  protected abstract ClaimStatus initialStatusFor(CampaignStepType firstStep);

  protected Claim.ClaimBuilder customize(
      final Claim.ClaimBuilder builder, final Campaign campaign) {
    return builder;
  }

  protected void validate(final Campaign campaign, final Claim partial) {}

  /**
   * Hook for a pre-creation rejection (e.g. a duplicate order) that must run before any slot is
   * consumed. No-op by default.
   */
  protected void checkDuplicate(final Claim partial) {}

  /**
   * Only called for a client-pre-scored flow; categories scored async via {@link ExtractionService}
   * skip it.
   */
  protected ExtractedScoredResult scoreExtractedDetails(
      final Claim partial,
      final Map<String, ScoredValue> extractedDetails,
      final Integer overallScore) {
    return new ExtractedScoredResult(extractedDetails, overallScore);
  }

  protected ClaimService claimService() {
    return this.claimService;
  }

  public final Claim create(
      final Claim partial,
      final byte[] screenshot,
      final String screenshotFilename,
      final String contentType,
      final Map<String, ScoredValue> extractedDetails,
      final Integer overallScore) {

    checkDuplicate(partial);

    final Deal deal = this.dealService.getById(partial.getDealId());
    final Campaign campaign = loadActiveCampaign(partial);
    validate(campaign, partial);
    decrementSlotOrThrow(deal);

    final CampaignStepType firstStep = this.campaignStepResolver.resolve(campaign).get(0);
    final String code =
        this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.CLAIM);

    final boolean preScored = extractedDetails != null || overallScore != null;
    final ExtractedScoredResult scored =
        preScored ? scoreExtractedDetails(partial, extractedDetails, overallScore) : null;

    final Claim.ClaimBuilder builder =
        partial.toBuilder()
            .code(code)
            .status(initialStatusFor(firstStep))
            .currentStep(firstStep)
            .score(scored == null ? null : scored.overallScore())
            .isDeleted(false)
            .createdBy(partial.getOwnerId())
            .updatedBy(partial.getOwnerId());
    final Claim saved = this.claimService.save(customize(builder, campaign).build());

    final Optional<ScreenshotType> screenshotType = screenshotTypeFor(firstStep);
    if (screenshotType.isPresent()) {
      final String storageKey =
          this.storageService.store("claims", screenshotFilename, contentType, screenshot);
      if (preScored) {
        saveScreenshot(
            saved.getId(),
            storageKey,
            screenshotType.get(),
            saved.getOwnerId(),
            scored.extractedResult(),
            scored.overallScore());
      } else {
        final ClaimScreenshot claimScreenshot =
            saveScreenshot(
                saved.getId(), storageKey, screenshotType.get(), saved.getOwnerId(), null, null);
        this.extractionService.submitJob(claimScreenshot.getId(), saved.getOwnerId());
      }
    }

    return saved;
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

  private void decrementSlotOrThrow(final Deal deal) {
    final int updated = this.campaignSlotService.decrementSlot(deal.getCampaignSlot().getId());
    if (updated == 0) {
      LOGGER.warn("All slots claimed for deal {}", deal.getId());
      throw new BusinessRuleViolationException("All slots have been claimed for this deal");
    }
  }

  private ClaimScreenshot saveScreenshot(
      final UUID claimId,
      final String storageKey,
      final ScreenshotType type,
      final UUID actorId,
      final Map<String, ScoredValue> extractedDetails,
      final Integer score) {
    return this.claimService.saveScreenshot(
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
}
