import type { Platform, CampaignType } from '../types'

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
  CAMPAIGN_TYPE_APP_REVIEW: 'App Review'
}

/** App-review campaigns run only on these platforms, and these platforms run only app-review campaigns. */
export const APP_STORE_PLATFORMS: Platform[] = ['PLATFORM_APPLE_APP_STORE', 'PLATFORM_GOOGLE_PLAY_STORE']
