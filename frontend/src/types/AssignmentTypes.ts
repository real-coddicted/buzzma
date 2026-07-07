import type { Platform, CampaignType, CampaignStatus } from './CampaignTypes'

export type { Platform, CampaignType, CampaignStatus }

export interface AssignmentSummary {
  id: string
  productName: string
  productImageUrl: string
  platform: Platform
  platformLabel: string
  dealType: CampaignType
  dealTypeLabel: string
  campaignStatus: CampaignStatus
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