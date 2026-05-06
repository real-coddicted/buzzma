import type { Platform, CampaignType } from './CampaignTypes'

export type { Platform, CampaignType }

export type DealStatus = 'explore' | 'in_progress' | 'completed'

export type DealTypeFilter     = CampaignType | 'all'
export type DealPlatformFilter = Platform     | 'all'

export type ClaimStep   = 1 | 2 | 3 | 4
export type ClaimStatus = 'pending' | 'approved' | 'rejected'

export interface ClaimOrderDetails {
  orderId:     string
  amount:      string
  productName: string
  soldBy:      string
  orderDate:   string
  accountName: string
  screenshotUrl?: string
}

export interface ClaimPayoutDetails {
  amount:      string
  disbursedOn: string
}

export interface DealTypeOption {
  value:       CampaignType
  label:       string
  activeClass: string
}

export interface Deal {
  id:                 string
  productName:        string
  productImageUrl:    string
  platform:           Platform
  platformLabel:      string
  dealType:           CampaignType
  dealTypeLabel:      string
  originalPricePaise: number
  offeredPricePaise:  number
  status:             DealStatus
  claimStep?:         ClaimStep
  claimStatus?:       ClaimStatus
  claimOrder?:        ClaimOrderDetails
  claimPayout?:       ClaimPayoutDetails
}
