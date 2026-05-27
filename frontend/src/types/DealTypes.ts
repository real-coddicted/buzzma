import type { Platform, CampaignType } from './CampaignTypes'

export type { Platform, CampaignType }

export type DealStatus = 'explore' | 'claimed'

export type DealTypeFilter     = CampaignType | 'all'
export type DealPlatformFilter = Platform     | 'all'

export interface DealTypeOption {
  value: CampaignType
  label: string
}

export interface Deal {
  id: string
  campaignId: string
  productName: string
  productImageUrl: string
  productUrl: string
  platform: Platform
  platformLabel: string
  dealType: CampaignType
  dealTypeLabel: string
  originalPricePaise: number
  offeredPricePaise: number
  sellerName?: string
  termsAndConditions?: string
  status: DealStatus
  currentStep?: number
}
