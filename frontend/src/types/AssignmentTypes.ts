import type { Platform, CampaignType } from './CampaignTypes'

export type { Platform, CampaignType }

export interface AssignmentItem {
  id: string
  productName: string
  productImageUrl: string
  platform: Platform
  platformLabel: string
  dealType: CampaignType
  dealTypeLabel: string
  offeredPricePaise: number
  slotsOffered: number
}
