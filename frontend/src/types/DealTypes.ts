import type { Platform, CampaignType } from './CampaignTypes'
import type { ClaimStatus } from './ClaimReviewTypes'

export type { Platform, CampaignType, ClaimStatus }

export type DealStatus = 'explore' | 'claimed'

export type DealTypeFilter     = CampaignType | 'all'
export type DealPlatformFilter = Platform     | 'all'

export interface DealTypeOption {
  value: CampaignType
  label: string
}

export interface Deal {
  id: string
  code?: string
  campaignId: string
  campaignCode?: string
  productName: string
  productImageUrl: string
  productUrl: string
  platform: Platform
  platformLabel: string
  dealType: CampaignType
  dealTypeLabel: string
  title?: string
  originalPricePaise: number
  offeredPricePaise: number
  sellerName?: string
  mediatorName?: string
  startDate?: string
  endDate?: string
  termsAndConditions?: string
  slotsAvailable?: number
  status: DealStatus
  currentStep?: number
  claimId?: string
  claimCode?: string
  claimStatus?: ClaimStatus
  screenshots?: Array<{ type?: string; verificationStatus?: string }>
  amountApprovedPaise?: number
}
