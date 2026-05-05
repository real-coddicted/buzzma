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
  platformLabel: string
  dealType: CampaignType
  dealTypeLabel: string
  originalPricePaise: number
  offeredPricePaise: number
  status: DealStatus
}
