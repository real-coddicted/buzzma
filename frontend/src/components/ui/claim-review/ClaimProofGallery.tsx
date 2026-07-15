import type { ScreenshotVerificationStatus } from '../../../types/ClaimReviewTypes'

export interface ExtractedField {
  key?: string         // raw extracted field key (e.g. 'order_id')
  label: string
  value: string        // extracted value
  matched: boolean
  indeterminate?: boolean
  score?: number | null
  campaignValue?: string    // from campaign / deal
  submittedValue?: string   // from buyer submission (ClaimReviewItem)
  submittedMismatch?: boolean // submittedValue differs from extracted value
}

export interface ClaimProofItem {
  id: string
  imageUrl: string
  imageAlt?: string
  type?: string
  score?: number
  fields: ExtractedField[]
  verificationStatus?: ScreenshotVerificationStatus
}
