export interface PaymentClaim {
  claimId: string
  campaignName: string
  brandName: string
  transactionAmount: number
  status: string
}

export interface PaymentBatch {
  id: string
  date: string
  agencyName: string
  paymentMode: string
  totalAmount: number
  claimCount: number
  proofStorageKey?: string
}

export interface PendingClaim {
  claimId: string
  campaignName: string
  submittedDate: string
  expectedAmount: number
  status: string
}

export interface PendingAgency {
  id: string
  agencyName: string
  agencyInitials: string
  pendingClaimCount: number
  totalPendingAmount: number
}
