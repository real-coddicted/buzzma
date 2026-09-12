package com.coddicted.buzzma.campaign.policy;

import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.constants.WellKnownPromotionCategoryPlatforms;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.util.Set;

/**
 * "Policy" class that governs campaign lifecycle.
 *
 * @author payam
 */
public final class CampaignPolicy {
  private CampaignPolicy() {}

  public static void validateCategoryAndPlatform(
      final PromotionCategory category, final Platform platform) {
    if (platform == null) {
      throw new BusinessRuleViolationException("Platform cannot be null");
    }
    final Set<Platform> allowedPlatforms =
        WellKnownPromotionCategoryPlatforms.getPlatforms(category);
    if (allowedPlatforms == null) {
      throw new BusinessRuleViolationException("Category not supported");
    }
    if (!allowedPlatforms.contains(platform)) {
      throw new BusinessRuleViolationException(
          category.getDisplayName() + " campaigns are not allowed on " + platform.getDisplayName());
    }
  }
}
