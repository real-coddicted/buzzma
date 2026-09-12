package com.coddicted.buzzma.shared.constants;

import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link PromotionCategory} to {@link Platform} mapping.
 *
 * @author payam
 */
public final class WellKnownPromotionCategoryPlatforms {
  private WellKnownPromotionCategoryPlatforms() {}

  private static final Map<PromotionCategory, Set<Platform>> PLATFORMS =
      new EnumMap<>(
          Map.of(
              PromotionCategory.ECOMMERCE,
              EnumSet.of(
                  Platform.PLATFORM_AMAZON,
                  Platform.PLATFORM_FLIPKART,
                  Platform.PLATFORM_NYKAA,
                  Platform.PLATFORM_MYNTRA,
                  Platform.PLATFORM_MEESHO),
              PromotionCategory.QUICK_COMMERCE,
              EnumSet.of(Platform.PLATFORM_BLINKIT, Platform.PLATFORM_ZEPTO),
              PromotionCategory.APP_PROMOTION,
              EnumSet.of(Platform.PLATFORM_APPLE_APP_STORE, Platform.PLATFORM_GOOGLE_PLAY_STORE),
              PromotionCategory.PAGE_PROMOTION,
              EnumSet.of(
                  Platform.PLATFORM_YOUTUBE,
                  Platform.PLATFORM_INSTAGRAM,
                  Platform.PLATFORM_GOOGLE_REVIEWS)));

  /** The platforms {@code category} is allowed to run on, or null if it isn't restricted. */
  public static Set<Platform> getPlatforms(final PromotionCategory category) {
    return PLATFORMS.get(category);
  }
}
