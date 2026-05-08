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
  productName: string
  productImageUrl: string
  platform: Platform
  platformLabel: string
  dealType: CampaignType
  dealTypeLabel: string
  originalPricePaise: number
  offeredPricePaise: number
  status: DealStatus
  currentStep?: number
}
