export interface PayoutUser {
  id: string
  name: string
  initials: string
  role: 'Mediator' | 'Buyer'
  upiId: string
  oldestClaimDate: string
  claimCount: number
  totalAmount: number
}

export interface PayoutClaim {
  id: string
  campaign: string
  brand: string
  platform: string
  approvedDate: string
  amount: number
}

export interface PaidPayee {
  id: string
  name: string
  initials: string
  role: 'Mediator' | 'Buyer'
  claimCount: number
  paymentCount: number
  totalAmount: number
  lastPaidDate: string
}

export interface MadePayment {
  id: string
  claimCount: number
  totalAmount: number
  paidAt: string
  paymentMethod: string
  screenshotStorageKey?: string
}

export interface PaymentSubmission {
  screenshot: File
  amountPaid: number
  paymentMethod: string
  otherMethod?: string
  utrRef?: string
  notes?: string
}

export const PAYMENT_METHODS = [
  { value: 'upi',   label: 'UPI' },
  { value: 'bank',  label: 'Bank Transfer' },
  { value: 'neft',  label: 'NEFT' },
  { value: 'imps',  label: 'IMPS' },
  { value: 'rtgs',  label: 'RTGS' },
  { value: 'cash',  label: 'Cash' },
  { value: 'other', label: 'Other' },
] as const
