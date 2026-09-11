package com.coddicted.buzzma.campaign.policy;

import com.coddicted.buzzma.campaign.entity.CampaignType;
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
   * App Promotion platforms only support Rating and Review campaigns - there's no order/discount
   * concept for an app. Unlike the old CAMPAIGN_TYPE_APP_REVIEW pairing, this is one-directional:
   * RATING/REVIEW are the same generic types Ecommerce already uses, so they stay valid on
   * non-app-store platforms too.
   */
  public static void validatePlatformAndCampaignType(
      final Platform platform, final CampaignType campaignType) {
    final boolean appPromotionPlatform = APP_PROMOTION_PLATFORMS.contains(platform);
    final boolean validAppPromotionType =
        campaignType == CampaignType.CAMPAIGN_TYPE_RATING
            || campaignType == CampaignType.CAMPAIGN_TYPE_REVIEW;
    if (appPromotionPlatform && !validAppPromotionType) {
      throw new BusinessRuleViolationException(
          "Apple App Store and Google Play Store campaigns must be of type Rating or Review");
    }
  }
}
