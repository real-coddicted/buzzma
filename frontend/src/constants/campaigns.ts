import type { Platform, CampaignType, PromotionCategory } from '../types'

export const PLATFORM_LABELS: Record<Platform, string> = {
  PLATFORM_AMAZON: 'Amazon',
  PLATFORM_FLIPKART: 'Flipkart',
  PLATFORM_NYKAA: 'Nykaa',
  PLATFORM_MYNTRA: 'Myntra',
  PLATFORM_MEESHO: 'Meesho',
  PLATFORM_APPLE_APP_STORE: 'Apple App Store',
  PLATFORM_GOOGLE_PLAY_STORE: 'Google Play Store',
}

export const CAMPAIGN_TYPE_LABELS: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING: 'Rating',
  CAMPAIGN_TYPE_REVIEW: 'Review',
  CAMPAIGN_TYPE_ORDER: 'Order',
  CAMPAIGN_TYPE_DISCOUNT: 'Discount',
  CAMPAIGN_TYPE_APP_REVIEW: 'App Review',
  CAMPAIGN_TYPE_EXCHANGE: 'Exchange'
}

/** App Promotion campaigns run only on these platforms. */
export const APP_STORE_PLATFORMS: Platform[] = ['PLATFORM_APPLE_APP_STORE', 'PLATFORM_GOOGLE_PLAY_STORE']

export const PROMOTION_CATEGORY_LABELS: Record<PromotionCategory, string> = {
  ECOMMERCE: 'Ecommerce Promotion',
  QUICK_COMMERCE: 'Quick Commerce Promotion',
  APP_PROMOTION: 'App Promotion',
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
 * Which campaign types are valid for each category. App Promotion only supports Rating and
 * Review (the backend rejects any other type paired with an app-store platform) - there's no
 * order/discount concept for an app, and CAMPAIGN_TYPE_APP_REVIEW is no longer offered.
 */
export const PROMOTION_CATEGORY_CAMPAIGN_TYPES: Record<PromotionCategory, CampaignType[]> = {
  ECOMMERCE: ['CAMPAIGN_TYPE_RATING', 'CAMPAIGN_TYPE_REVIEW', 'CAMPAIGN_TYPE_ORDER', 'CAMPAIGN_TYPE_DISCOUNT', 'CAMPAIGN_TYPE_EXCHANGE'],
  QUICK_COMMERCE: ['CAMPAIGN_TYPE_RATING', 'CAMPAIGN_TYPE_REVIEW', 'CAMPAIGN_TYPE_ORDER', 'CAMPAIGN_TYPE_DISCOUNT', 'CAMPAIGN_TYPE_EXCHANGE'],
  APP_PROMOTION: ['CAMPAIGN_TYPE_RATING', 'CAMPAIGN_TYPE_REVIEW'],
}

/**
 * Which required-screenshot steps are selectable for each category. App Promotion only offers
 * Rating and Review; DOWNLOAD_INSTALL isn't shown here at all because it's forced server-side the
 * same way ORDER is forced for the other categories (see CampaignProcessor.normalizeRequiredSteps),
 * not something the campaign owner chooses.
 */
export const PROMOTION_CATEGORY_STEPS: Record<PromotionCategory, string[]> = {
  ECOMMERCE: ['ORDER', 'DELIVERY', 'RATING', 'REVIEW', 'SELLER_FEEDBACK', 'RETURN_WINDOW'],
  QUICK_COMMERCE: ['ORDER', 'DELIVERY', 'RATING', 'REVIEW', 'SELLER_FEEDBACK', 'RETURN_WINDOW'],
  APP_PROMOTION: ['RATING', 'REVIEW'],
}
