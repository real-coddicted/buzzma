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

/** App Promotion claim creation template. */
@Component
public class AppPromotionClaimCreationTemplate extends ClaimCreationTemplate {

  public AppPromotionClaimCreationTemplate(
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
    return PromotionCategory.APP_PROMOTION;
  }

  @Override
  protected Optional<ScreenshotType> screenshotTypeFor(final CampaignStepType firstStep) {
    return switch (firstStep) {
      case DOWNLOAD_INSTALL -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_DOWNLOAD_INSTALL);
      case REVIEW -> Optional.of(ScreenshotType.SCREENSHOT_TYPE_REVIEW);
      default ->
          throw new IllegalStateException(
              "Unexpected first step for App Promotion campaign: " + firstStep);
    };
  }

  @Override
  protected ClaimStatus initialStatusFor(final CampaignStepType firstStep) {
    return switch (firstStep) {
      case DOWNLOAD_INSTALL -> ClaimStatus.DOWNLOADED_AND_INSTALLED;
      case REVIEW -> ClaimStatus.REVIEW_SUBMITTED;
      default ->
          throw new IllegalStateException(
              "Unexpected first step for App Promotion campaign: " + firstStep);
    };
  }

  @Override
  protected Claim.ClaimBuilder customize(
      final Claim.ClaimBuilder builder, final Campaign campaign) {
    return builder.ecommerceOrderId("NA").platform(campaign.getPlatform());
  }
}
