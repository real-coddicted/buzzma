import type { Platform, CampaignType } from './CampaignTypes'

export type { Platform, CampaignType }

export type DealStatus = 'explore' | 'in_progress' | 'completed'

export interface DealTypeOption {
  value: CampaignType
  label: string
}

export interface Deal {
  id: string
  productName: string
  productImageUrl: string
  platform: Platform
  dealType: CampaignType
  originalPricePaise: number
  offeredPricePaise: number
  status: DealStatus
}
