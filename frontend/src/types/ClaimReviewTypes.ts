export type ClaimStatus = 'in-progress' | 'completed'
export type ReviewStatus = 'pending' | 'in-review' | 'approved' | 'rejected'
export type ApprovalMethod = 'manual' | 'auto'

export interface ClaimScreenshotItem {
  id: string
  storageKey: string
  type: string
  score?: number
  extractedDetails?: Record<string, string>
}

export interface ClaimReviewItem {
  id: string
  campaignId: string
  campaignName: string
  orderId: string
  orderDate: string
  mediatorName: string
  claimStatus: ClaimStatus
  reviewStatus: ReviewStatus
  approvalMethod: ApprovalMethod
  mediatorVerified: boolean
  matchPct: number
  // Extended detail fields (from GET /claims/{id})
  accountName?: string
  productName?: string
  sellerName?: string
  amountPaise?: number
  reviewUrl?: string
  currentStep?: number
  reviewerComments?: string
  screenshots?: ClaimScreenshotItem[]
}
