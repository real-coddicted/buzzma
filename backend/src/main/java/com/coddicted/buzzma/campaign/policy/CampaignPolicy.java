package com.coddicted.buzzma.campaign.policy;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.util.EnumSet;
import java.util.Set;

public final class CampaignPolicy {
  private CampaignPolicy() {}

  /**
   * The platforms that make up the App Promotion category. Kept as a local set rather than a {@code
   * Platform.category} property, since a platform can genuinely belong to more than one category
   * (e.g. Instagram spans both Social Media Content and Page Promotion) - a single-valued property
   * on {@code Platform} would be wrong the moment a second category shares a platform, so this
   * stays narrowly scoped to what's actually true today.
   */
  private static final Set<Platform> APP_PROMOTION_PLATFORMS =
      EnumSet.of(Platform.PLATFORM_APPLE_APP_STORE, Platform.PLATFORM_GOOGLE_PLAY_STORE);

  /**
   * Regular-type campaigns only make sense on an app store, and the app stores only host
   * regular-type campaigns — the pairing is enforced both ways. Likewise, Regular is exclusively
   * the App Promotion category's campaign type, so the two must agree.
   */
  public static void validatePlatformCampaignTypeAndCategory(
      final Platform platform, final CampaignType campaignType, final PromotionCategory category) {
    final boolean appPromotionPlatform = APP_PROMOTION_PLATFORMS.contains(platform);
    final boolean regularType = campaignType == CampaignType.CAMPAIGN_TYPE_REGULAR;
    if (regularType && !appPromotionPlatform) {
      throw new BusinessRuleViolationException(
          "Regular campaigns are only allowed on Apple App Store or Google Play Store");
    }
    if (appPromotionPlatform && !regularType) {
      throw new BusinessRuleViolationException(
          "Apple App Store and Google Play Store campaigns must be of type Regular");
    }

    final boolean appPromotionCategory = category == PromotionCategory.APP_PROMOTION;
    if (appPromotionCategory && !regularType) {
      throw new BusinessRuleViolationException("App Promotion campaigns must be of type Regular");
    }
    if (regularType && !appPromotionCategory) {
      throw new BusinessRuleViolationException(
          "Only App Promotion campaigns can be of type Regular");
    }
  }
}
