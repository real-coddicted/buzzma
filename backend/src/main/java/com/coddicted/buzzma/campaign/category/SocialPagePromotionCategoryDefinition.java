package com.coddicted.buzzma.campaign.category;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SocialPagePromotionCategoryDefinition implements PromotionCategoryDefinition {

  private static final Map<Platform, Set<CampaignStepType>> STEPS_BY_PLATFORM =
      Map.of(
          Platform.PLATFORM_GOOGLE_REVIEWS,
          Set.of(CampaignStepType.REVIEW, CampaignStepType.RATING),
          Platform.PLATFORM_INSTAGRAM,
          Set.of(
              CampaignStepType.VIEW,
              CampaignStepType.LIKE,
              CampaignStepType.FOLLOW,
              CampaignStepType.COMMENT),
          Platform.PLATFORM_YOUTUBE,
          Set.of(
              CampaignStepType.SUBSCRIBE,
              CampaignStepType.VIEW,
              CampaignStepType.LIKE,
              CampaignStepType.COMMENT),
          Platform.PLATFORM_OTHER,
          Set.of(
              CampaignStepType.REVIEW,
              CampaignStepType.RATING,
              CampaignStepType.VIEW,
              CampaignStepType.LIKE,
              CampaignStepType.FOLLOW,
              CampaignStepType.COMMENT,
              CampaignStepType.SUBSCRIBE));

  @Override
  public PromotionCategory category() {
    return PromotionCategory.SOCIAL_PAGE_PROMOTION;
  }

  @Override
  public Set<Platform> allowedPlatforms() {
    return Set.of(
        Platform.PLATFORM_GOOGLE_REVIEWS,
        Platform.PLATFORM_INSTAGRAM,
        Platform.PLATFORM_YOUTUBE,
        Platform.PLATFORM_OTHER);
  }

  @Override
  public Set<CampaignType> allowedCampaignTypes() {
    return Set.of(CampaignType.CAMPAIGN_TYPE_REGULAR);
  }

  @Override
  public List<CampaignStepType> allowedSteps() {
    return List.of(
        CampaignStepType.REVIEW,
        CampaignStepType.RATING,
        CampaignStepType.VIEW,
        CampaignStepType.LIKE,
        CampaignStepType.FOLLOW,
        CampaignStepType.COMMENT,
        CampaignStepType.SUBSCRIBE);
  }

  @Override
  public Set<CampaignStepType> allowedStepsForPlatform(final Platform platform) {
    return STEPS_BY_PLATFORM.getOrDefault(platform, Set.copyOf(allowedSteps()));
  }

  @Override
  public Optional<CampaignStepType> forcedStep() {
    return Optional.empty();
  }
}
