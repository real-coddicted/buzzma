package com.coddicted.buzzma.campaign.policy;

import com.coddicted.buzzma.campaign.category.PromotionCategoryDefinition;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class CampaignPolicy {
  private CampaignPolicy() {}

  /**
   * A campaign's platform and campaign type must both be among those its promotion category's
   * {@link PromotionCategoryDefinition} allows.
   */
  public static void validatePlatformAndCampaignType(
      final Platform platform,
      final CampaignType campaignType,
      final PromotionCategoryDefinition categoryDefinition) {
    if (!categoryDefinition.allowedPlatforms().contains(platform)) {
      throw new BusinessRuleViolationException(
          categoryDefinition.category().getDisplayName()
              + " campaigns must use one of these platforms: "
              + categoryDefinition.allowedPlatforms().stream()
                  .sorted(Comparator.comparingInt(Enum::ordinal))
                  .map(Platform::getDisplayName)
                  .collect(Collectors.joining(", ")));
    }
    if (!categoryDefinition.allowedCampaignTypes().contains(campaignType)) {
      throw new BusinessRuleViolationException(
          categoryDefinition.category().getDisplayName()
              + " campaigns must be one of these campaign types: "
              + categoryDefinition.allowedCampaignTypes().stream()
                  .sorted(Comparator.comparingInt(Enum::ordinal))
                  .map(CampaignType::name)
                  .collect(Collectors.joining(", ")));
    }
  }
}
