import type { components } from './api'
import type { Platform } from './CampaignTypes'

type ScoredValue = components['schemas']['ScoredValue']

export type ClaimStatus = 'in-progress' | 'completed'
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
  amountPaise?: number
  reviewUrl?: string
  currentStep?: number
  reviewerComments?: string
  screenshots?: ClaimScreenshotItem[]
  isUnderReview?: boolean
}
