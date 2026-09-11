package com.coddicted.buzzma.claim.strategy;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import java.util.Optional;

public interface ClaimCreationStrategy {

  PromotionCategory category();

  Optional<ScreenshotType> screenshotTypeFor(CampaignStepType firstStep);

  ClaimStatus initialStatusFor(CampaignStepType firstStep);

  default Claim.ClaimBuilder customize(final Claim.ClaimBuilder builder, final Campaign campaign) {
    return builder;
  }

  default void validate(final Campaign campaign, final Claim partial) {}

  default Claim create(
      final ClaimCreationContext ctx,
      final Claim partial,
      final byte[] screenshot,
      final String screenshotFilename,
      final String contentType) {

    final Deal deal = ctx.dealService().getById(partial.getDealId());
    final Campaign campaign = ctx.loadActiveCampaign(partial);
    validate(campaign, partial);
    ctx.decrementSlotOrThrow(deal);

    final CampaignStepType firstStep = ctx.campaignStepResolver().resolve(campaign).get(0);
    final String code =
        ctx.codeGenerationService().generateCodeFromSequence(WellKnownSequences.CLAIM);

    final Claim.ClaimBuilder builder =
        partial.toBuilder()
            .code(code)
            .status(initialStatusFor(firstStep))
            .currentStep(firstStep)
            .isDeleted(false)
            .createdBy(partial.getOwnerId())
            .updatedBy(partial.getOwnerId());
    final Claim saved = ctx.claimService().save(customize(builder, campaign).build());

    final Optional<ScreenshotType> screenshotType = screenshotTypeFor(firstStep);
    if (screenshotType.isPresent()) {
      final String storageKey =
          ctx.storageService().store("claims", screenshotFilename, contentType, screenshot);
      final ClaimScreenshot claimScreenshot =
          ctx.saveScreenshot(saved.getId(), storageKey, screenshotType.get(), saved.getOwnerId());
      ctx.extractionService().submitJob(claimScreenshot.getId(), saved.getOwnerId());
    }

    return saved;
  }
}
