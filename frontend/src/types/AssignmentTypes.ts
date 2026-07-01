import type { Platform, CampaignType } from './CampaignTypes'

export type { Platform, CampaignType }

export interface AssignmentSummary {
  id: string
  productName: string
  productImageUrl: string
  platform: Platform
  platformLabel: string
  dealType: CampaignType
  dealTypeLabel: string
  originalPricePaise: number
  offeredPricePaise: number
  slotsOffered: number
}

export interface AssignmentItem extends AssignmentSummary {
  campaignId: string
  productUrl: string
  commissionOfferedPaise: number
  sellerName?: string
  termsAndConditions?: string
}