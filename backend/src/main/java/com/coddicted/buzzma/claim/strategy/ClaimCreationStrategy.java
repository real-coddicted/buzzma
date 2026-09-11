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

/**
 * Template Method for claim creation, one implementation per {@link PromotionCategory}. The fixed
 * mechanics (active-campaign check, slot enforcement, code generation, saving the claim, and - only
 * if this category's first step needs one - storing a screenshot and submitting it for extraction)
 * live once in {@link #create}. Each category supplies only the handful of things that actually
 * vary.
 *
 * <p>The first step itself is <em>not</em> hardcoded per category - it's read from {@code
 * campaignStepResolver.resolve(campaign).get(0)}, i.e. whatever the campaign's own {@code
 * requiredSteps} resolves to first. This matters for a category like Social Media Page Promotion,
 * where the campaign owner picks which one of several equally-valid actions (visit/like/follow/
 * subscribe) is required - there's no single fixed "first step" for that category, only for a given
 * campaign. {@link #screenshotTypeFor} and {@link #initialStatusFor} are therefore functions of the
 * resolved step, not category-wide constants, even for categories (App Promotion, today) that only
 * ever resolve to one possible step in practice.
 *
 * <p>Implementations are picked up automatically by Spring's {@code List<ClaimCreationStrategy>}
 * injection (see the registry that will dispatch on {@link #category()}) - adding a new category is
 * adding one new {@code @Component} implementing this interface, nothing else to wire up.
 */
public interface ClaimCreationStrategy {

  /** Which {@link PromotionCategory} this strategy handles. */
  PromotionCategory category();

  /**
   * The proof required for the given (already-resolved) first step. Empty means this step needs no
   * screenshot at all to create the claim - e.g. a category whose proof is a submitted link rather
   * than an image, or a category with no proof requirement at claim-creation time.
   */
  Optional<ScreenshotType> screenshotTypeFor(CampaignStepType firstStep);

  /** The claim's initial status once created at the given first step. */
  ClaimStatus initialStatusFor(CampaignStepType firstStep);

  /**
   * Hook for whatever category-specific fields belong on the claim beyond the generic ones the
   * template already sets (code, status, currentStep, audit fields) - e.g. a hardcoded {@code
   * ecommerceOrderId} placeholder for a category with no real order ID. No-op by default.
   */
  default Claim.ClaimBuilder customize(final Claim.ClaimBuilder builder, final Campaign campaign) {
    return builder;
  }

  /**
   * Hook for category-specific validation beyond the generic active-campaign/slot checks - e.g.
   * exchange-product validation, which only applies to {@code CampaignType.CAMPAIGN_TYPE_EXCHANGE}
   * campaigns within the Ecommerce category. Called after the campaign is loaded but before the
   * slot is consumed, so a failed check doesn't burn capacity. No-op by default; throw a {@code
   * BusinessRuleViolationException} to reject.
   */
  default void validate(final Campaign campaign, final Claim partial) {}

  /**
   * The template method - identical for every category, never overridden. {@code partial} is the
   * claim as built by the category's own controller from its own request DTO (carrying whatever
   * category-specific input fields, plus campaignId/dealId/ownerId); {@code screenshot} may be
   * {@code null} exactly when {@link #screenshotTypeFor} returns empty for the resolved first step.
   */
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
    final Claim saved = ctx.claimRepository().save(customize(builder, campaign).build());

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
