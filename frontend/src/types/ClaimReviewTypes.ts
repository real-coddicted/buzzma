import type { components } from './api'
import type { Platform } from './CampaignTypes'

type ScoredValue = components['schemas']['ScoredValue']

export type ClaimStatus =
  | 'CREATED'
  | 'REDIRECTED'
  | 'ORDERED'
  | 'RATING_SUBMITTED'
  | 'REVIEW_SUBMITTED'
  | 'PROOF_SUBMITTED'
  | 'PROOF_REJECTED'
  | 'UNDER_REVIEW'
  | 'ADDITIONAL_PROOF_REQUESTED'
  | 'UNDER_BRAND_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'REWARD_PENDING'
  | 'COMPLETED'
  | 'FAILED'
export type ReviewStatus = 'pending' | 'in-review' | 'approved' | 'rejected' | 'objected'
export type ApprovalMethod = 'manual' | 'auto'

export type ScreenshotVerificationStatus =
  | 'SCREENSHOT_VERIFICATION_STATUS_PENDING'
  | 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED'
  | 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'

export interface ClaimScreenshotItem {
  id: string
  storageKey: string
  type: string
  score?: number
  extractedDetails?: Record<string, ScoredValue>
  verificationStatus?: ScreenshotVerificationStatus
  reviewerComments?: string
}

export interface ClaimReviewItem {
  id: string
  campaignId: string
  campaignName: string
  orderId: string
  orderDate: string
  mediatorId: string
  mediatorName: string
  buyerName: string
  claimStatus: ClaimStatus
  reviewStatus: ReviewStatus
  approvalMethod: ApprovalMethod
  mediatorVerified: boolean
  matchPct: number
  platform: Platform
  brandName: string
  // Extended detail fields (from GET /claims/{id})
  accountName?: string
  orderedBy?: string
  productName?: string
  sellerName?: string
  productPricePaise?: number
  campaignPricePaise?: number
  amountPaise?: number
  amountApprovedPaise?: number
  reviewUrl?: string
  currentStep?: number
  reviewerComments?: string
  screenshots?: ClaimScreenshotItem[]
  isUnderReview?: boolean
  isUnderBrandReview?: boolean
}
