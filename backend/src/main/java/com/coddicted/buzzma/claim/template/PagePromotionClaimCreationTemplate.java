package com.coddicted.buzzma.claim.template;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Page Promotion claim creation template. */
@Component
public class PagePromotionClaimCreationTemplate extends ClaimCreationTemplate {

  public PagePromotionClaimCreationTemplate(
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
    return PromotionCategory.PAGE_PROMOTION;
  }

  @Override
  protected Optional<ScreenshotType> screenshotTypeFor(final CampaignStepType firstStep) {
    return switch (firstStep) {
      case SUBSCRIBE_CHANNEL -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_SUBSCRIBE_CHANNEL);
      case FOLLOW -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_FOLLOW);
      case RATING -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_RATING);
      case REVIEW -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_REVIEW);
      default ->
          throw new IllegalStateException(
              "Unexpected first step for Page Promotion campaign: " + firstStep);
    };
  }

  @Override
  protected ClaimStatus initialStatusFor(final CampaignStepType firstStep) {
    return switch (firstStep) {
      case SUBSCRIBE_CHANNEL -> ClaimStatus.SUBSCRIBED;
      case FOLLOW -> ClaimStatus.FOLLOWED;
      case RATING -> ClaimStatus.RATING_SUBMITTED;
      case REVIEW -> ClaimStatus.REVIEW_SUBMITTED;
      default ->
          throw new IllegalStateException(
              "Unexpected first step for Page Promotion campaign: " + firstStep);
    };
  }

  /***
   * Setting ecommerceId to NA for page promotions.
   * @param builder {@link Claim.ClaimBuilder}
   * @param campaign {@link Campaign}
   * @return updated {@link Claim.ClaimBuilder}
   */
  @Override
  protected Claim.ClaimBuilder customize(
      final Claim.ClaimBuilder builder, final Campaign campaign) {
    return builder.ecommerceOrderId("NA").platform(campaign.getPlatform());
  }
}
