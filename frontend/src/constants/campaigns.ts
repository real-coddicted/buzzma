import type { Platform, CampaignType, PromotionCategory } from '../types'

export const PLATFORM_LABELS: Record<Platform, string> = {
  PLATFORM_AMAZON: 'Amazon',
  PLATFORM_FLIPKART: 'Flipkart',
  PLATFORM_NYKAA: 'Nykaa',
  PLATFORM_MYNTRA: 'Myntra',
  PLATFORM_MEESHO: 'Meesho',
  PLATFORM_BLINKIT: 'Blinkit',
  PLATFORM_ZEPTO: 'Zepto',
  PLATFORM_APPLE_APP_STORE: 'Apple App Store',
  PLATFORM_GOOGLE_PLAY_STORE: 'Google Play Store',
  PLATFORM_YOUTUBE: 'YouTube',
  PLATFORM_INSTAGRAM: 'Instagram',
  PLATFORM_GOOGLE_REVIEWS: 'Google Reviews',
  PLATFORM_OTHER: 'Other',
}

export const CAMPAIGN_TYPE_LABELS: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING: 'Rating',
  CAMPAIGN_TYPE_REVIEW: 'Review',
  CAMPAIGN_TYPE_ORDER: 'Order',
  CAMPAIGN_TYPE_DISCOUNT: 'Discount',
  CAMPAIGN_TYPE_REGULAR: 'Regular',
  CAMPAIGN_TYPE_EXCHANGE: 'Exchange'
}

/** App Promotion campaigns run only on these platforms. */
export const APP_STORE_PLATFORMS: Platform[] = ['PLATFORM_APPLE_APP_STORE', 'PLATFORM_GOOGLE_PLAY_STORE']

export const PROMOTION_CATEGORY_LABELS: Record<PromotionCategory, string> = {
  ECOMMERCE: 'E-commerce',
  QUICK_COMMERCE: 'Quick Commerce Promotion',
  APP_PROMOTION: 'App Review',
}

/**
 * Which platforms are valid for each category. QUICK_COMMERCE is intentionally empty - Blinkit/Zepto
 * don't exist in the Platform enum yet, so there's nothing honest to offer in the platform dropdown
 * until that lands.
 */
export const PROMOTION_CATEGORY_PLATFORMS: Record<PromotionCategory, Platform[]> = {
  ECOMMERCE: ['PLATFORM_AMAZON', 'PLATFORM_FLIPKART', 'PLATFORM_NYKAA', 'PLATFORM_MYNTRA', 'PLATFORM_MEESHO'],
  QUICK_COMMERCE: [],
  APP_PROMOTION: APP_STORE_PLATFORMS,
}

/**
 * Which campaign types are valid for each category. App Promotion always uses CAMPAIGN_TYPE_REGULAR -
 * campaignType represents the shape of the offering (regular/exchange), not a task, so there's no
 * per-task choice here the way Ecommerce has.
 */
export const PROMOTION_CATEGORY_CAMPAIGN_TYPES: Record<PromotionCategory, CampaignType[]> = {
  ECOMMERCE: ['CAMPAIGN_TYPE_RATING', 'CAMPAIGN_TYPE_REVIEW', 'CAMPAIGN_TYPE_ORDER', 'CAMPAIGN_TYPE_DISCOUNT', 'CAMPAIGN_TYPE_EXCHANGE'],
  QUICK_COMMERCE: ['CAMPAIGN_TYPE_RATING', 'CAMPAIGN_TYPE_REVIEW', 'CAMPAIGN_TYPE_ORDER', 'CAMPAIGN_TYPE_DISCOUNT', 'CAMPAIGN_TYPE_EXCHANGE'],
  APP_PROMOTION: ['CAMPAIGN_TYPE_REGULAR'],
}

/**
 * Which required-screenshot steps are selectable for each category. The forced step (see {@link
 * PROMOTION_CATEGORY_FORCED_STEP}) is included here too, so it's shown in the list (pre-checked and
 * disabled) rather than hidden - the campaign owner should still see it's part of the flow, even
 * though they can't opt out of it.
 */
export const PROMOTION_CATEGORY_STEPS: Record<PromotionCategory, string[]> = {
  ECOMMERCE: ['ORDER', 'DELIVERY', 'RATING', 'REVIEW', 'SELLER_FEEDBACK', 'RETURN_WINDOW'],
  QUICK_COMMERCE: ['ORDER', 'DELIVERY', 'RATING', 'REVIEW', 'SELLER_FEEDBACK', 'RETURN_WINDOW'],
  APP_PROMOTION: ['DOWNLOAD_INSTALL', 'RATING', 'REVIEW'],
}

/**
 * The step every campaign in a category is forced to require, mirroring
 * CampaignProcessor.normalizeRequiredSteps on the backend - ORDER for most categories,
 * DOWNLOAD_INSTALL for App Promotion. Always included in requiredSteps and shown disabled in the
 * UI, never something the campaign owner can uncheck.
 */
export const PROMOTION_CATEGORY_FORCED_STEP: Record<PromotionCategory, string> = {
  ECOMMERCE: 'ORDER',
  QUICK_COMMERCE: 'ORDER',
  APP_PROMOTION: 'DOWNLOAD_INSTALL',
}
