package com.coddicted.buzzma.claim.template;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.policy.ClaimPolicy;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.claim.utils.ClaimScreenshotScorerUtils;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Claim creation for Ecommerce Promotion Category. */
@Component
public class EcommerceClaimCreationTemplate extends ClaimCreationTemplate {

  public EcommerceClaimCreationTemplate(
      final DealService dealService,
      final CampaignService campaignService,
      final CampaignSlotService campaignSlotService,
      final CampaignStepResolver campaignStepResolver,
      final StorageService storageService,
      final ExtractionService extractionService,
      final CodeGenerationService codeGenerationService,
      final ClaimService claimService) {
    super(
        dealService,
        campaignService,
        campaignSlotService,
        campaignStepResolver,
        storageService,
        extractionService,
        codeGenerationService,
        claimService);
  }

  @Override
  public PromotionCategory category() {
    return PromotionCategory.ECOMMERCE;
  }

  @Override
  protected void checkDuplicate(final Claim partial) {
    final String ecommerceOrderId = partial.getEcommerceOrderId();
    final Platform platform = partial.getPlatform();
    if (claimService().existsActiveClaimForOrder(ecommerceOrderId, platform)) {
      throw new BusinessRuleViolationException("Claim with this Order ID has already been placed");
    }
  }

  @Override
  protected void validate(final Campaign campaign, final Claim partial) {
    ClaimPolicy.validateExchangeProduct(campaign, partial);
  }

  @Override
  protected Optional<ScreenshotType> screenshotTypeFor(final CampaignStepType firstStep) {
    return switch (firstStep) {
      case ORDER -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_ORDER);
      default ->
          throw new IllegalStateException(
              "Unexpected first step for Ecommerce campaign: " + firstStep);
    };
  }

  @Override
  protected ClaimStatus initialStatusFor(final CampaignStepType firstStep) {
    return switch (firstStep) {
      case ORDER -> ClaimStatus.ORDERED;
      default ->
          throw new IllegalStateException(
              "Unexpected first step for Ecommerce campaign: " + firstStep);
    };
  }

  @Override
  protected ExtractedScoredResult scoreExtractedDetails(
      final Claim partial,
      final Map<String, ScoredValue> extractedDetails,
      final Integer overallScore) {
    return ClaimScreenshotScorerUtils.updateExtractedDataForMatchWithManualEntryInOrder(
        partial, extractedDetails, overallScore);
  }

  @Override
  protected Claim.ClaimBuilder customize(
      final Claim.ClaimBuilder builder, final Campaign campaign) {
    return campaign.getSellerName() != null ? builder : builder.sellerName(null);
  }
}
