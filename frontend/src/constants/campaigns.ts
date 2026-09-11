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

/** App-review campaigns run only on these platforms, and these platforms run only app-review campaigns. */
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
