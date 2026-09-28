package com.coddicted.buzzma.campaign.category;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AppPromotionCategoryDefinition implements PromotionCategoryDefinition {

  @Override
  public PromotionCategory category() {
    return PromotionCategory.APP_PROMOTION;
  }

  @Override
  public Set<Platform> allowedPlatforms() {
    return Set.of(Platform.PLATFORM_APPLE_APP_STORE, Platform.PLATFORM_GOOGLE_PLAY_STORE);
  }

  @Override
  public Set<CampaignType> allowedCampaignTypes() {
    return Set.of(CampaignType.CAMPAIGN_TYPE_REGULAR);
  }

  @Override
  public List<CampaignStepType> allowedSteps() {
    return List.of(
        CampaignStepType.DOWNLOAD_INSTALL, CampaignStepType.RATING, CampaignStepType.REVIEW);
  }

  @Override
  public Optional<CampaignStepType> forcedStep() {
    return Optional.of(CampaignStepType.DOWNLOAD_INSTALL);
  }
}
