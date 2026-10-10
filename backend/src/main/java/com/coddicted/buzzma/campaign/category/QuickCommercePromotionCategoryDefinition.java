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
public class QuickCommercePromotionCategoryDefinition implements PromotionCategoryDefinition {

  @Override
  public PromotionCategory category() {
    return PromotionCategory.QUICK_COMMERCE;
  }

  @Override
  public Set<Platform> allowedPlatforms() {
    return Set.of(Platform.PLATFORM_BIGBASKET, Platform.PLATFORM_BLINKIT, Platform.PLATFORM_ZEPTO);
  }

  @Override
  public Set<CampaignType> allowedCampaignTypes() {
    return Set.of(
        CampaignType.CAMPAIGN_TYPE_RATING,
        CampaignType.CAMPAIGN_TYPE_REVIEW,
        CampaignType.CAMPAIGN_TYPE_ORDER,
        CampaignType.CAMPAIGN_TYPE_DISCOUNT);
  }

  @Override
  public List<CampaignStepType> allowedSteps() {
    return List.of(
        CampaignStepType.ORDER,
        CampaignStepType.DELIVERY,
        CampaignStepType.RATING,
        CampaignStepType.REVIEW);
  }

  @Override
  public Optional<CampaignStepType> forcedStep() {
    return Optional.of(CampaignStepType.ORDER);
  }
}
