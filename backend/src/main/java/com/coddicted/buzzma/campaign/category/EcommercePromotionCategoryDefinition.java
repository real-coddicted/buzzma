package com.coddicted.buzzma.campaign.category;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EcommercePromotionCategoryDefinition implements PromotionCategoryDefinition {

  @Override
  public PromotionCategory category() {
    return PromotionCategory.ECOMMERCE;
  }

  @Override
  public Set<Platform> allowedPlatforms() {
    return Set.of(
        Platform.PLATFORM_AMAZON,
        Platform.PLATFORM_FLIPKART,
        Platform.PLATFORM_NYKAA,
        Platform.PLATFORM_MYNTRA,
        Platform.PLATFORM_MEESHO);
  }

  @Override
  public Set<CampaignType> allowedCampaignTypes() {
    return Set.of(
        CampaignType.CAMPAIGN_TYPE_RATING,
        CampaignType.CAMPAIGN_TYPE_REVIEW,
        CampaignType.CAMPAIGN_TYPE_ORDER,
        CampaignType.CAMPAIGN_TYPE_DISCOUNT,
        CampaignType.CAMPAIGN_TYPE_EXCHANGE);
  }

  @Override
  public List<CampaignStepType> allowedSteps() {
    return List.of(
        CampaignStepType.ORDER,
        CampaignStepType.DELIVERY,
        CampaignStepType.RATING,
        CampaignStepType.REVIEW,
        CampaignStepType.SELLER_FEEDBACK,
        CampaignStepType.RETURN_WINDOW);
  }

  @Override
  public CampaignStepType forcedStep() {
    return CampaignStepType.ORDER;
  }
}
