import type { PaymentBatch, PaymentClaim, PendingAgency, PendingClaim } from '../types/MyPaymentsTypes'

const MOCK_BATCHES: PaymentBatch[] = [
  { id: 'b1', date: 'July 22, 2026', agencyName: 'Blinkit Agency', paymentMode: 'Bank Transfer', totalAmount: 12500, claimCount: 5 },
  { id: 'b2', date: 'July 10, 2026', agencyName: 'Swiggy Partners', paymentMode: 'UPI', totalAmount: 8200, claimCount: 3 },
  { id: 'b3', date: 'June 28, 2026', agencyName: 'Zepto Ads', paymentMode: 'Bank Transfer', totalAmount: 15000, claimCount: 6 },
  { id: 'b4', date: 'June 15, 2026', agencyName: 'Blinkit Agency', paymentMode: 'UPI', totalAmount: 9800, claimCount: 4 },
  { id: 'b5', date: 'May 30, 2026', agencyName: 'Myntra Influence', paymentMode: 'Bank Transfer', totalAmount: 11000, claimCount: 4 },
  { id: 'b6', date: 'May 18, 2026', agencyName: 'Swiggy Partners', paymentMode: 'UPI', totalAmount: 6400, claimCount: 2 },
  { id: 'b7', date: 'May 5, 2026', agencyName: 'Zepto Ads', paymentMode: 'Bank Transfer', totalAmount: 7200, claimCount: 3 },
  { id: 'b8', date: 'April 22, 2026', agencyName: 'Amazon Connects', paymentMode: 'Bank Transfer', totalAmount: 4800, claimCount: 2 },
  { id: 'b9', date: 'April 10, 2026', agencyName: 'Blinkit Agency', paymentMode: 'NEFT', totalAmount: 3900, claimCount: 2 },
  { id: 'b10', date: 'March 28, 2026', agencyName: 'Myntra Influence', paymentMode: 'UPI', totalAmount: 2100, claimCount: 1 },
  { id: 'b11', date: 'March 14, 2026', agencyName: 'Swiggy Partners', paymentMode: 'Bank Transfer', totalAmount: 1950, claimCount: 1 },
  { id: 'b12', date: 'January 18, 2026', agencyName: 'Zepto Ads', paymentMode: 'UPI', totalAmount: 1381, claimCount: 1 },
]

const MOCK_CLAIMS: Record<string, PaymentClaim[]> = {
  b1: [
    { claimId: 'CLM-1042', campaignName: 'Summer Essentials Q3', brandName: 'Dove', transactionAmount: 2500, status: 'Paid' },
    { claimId: 'CLM-1039', campaignName: 'Back to School', brandName: 'Classmate', transactionAmount: 3200, status: 'Paid' },
    { claimId: 'CLM-1035', campaignName: 'Festival Ready', brandName: 'Haldirams', transactionAmount: 1800, status: 'Paid' },
    { claimId: 'CLM-1031', campaignName: 'Summer Essentials Q3', brandName: 'Dove', transactionAmount: 2800, status: 'Paid' },
    { claimId: 'CLM-1028', campaignName: 'Monsoon Fresh', brandName: 'Lifebuoy', transactionAmount: 2200, status: 'Paid' },
  ],
  b2: [
    { claimId: 'CLM-1025', campaignName: 'Gourmet Week', brandName: 'Maggi', transactionAmount: 3100, status: 'Paid' },
    { claimId: 'CLM-1022', campaignName: 'Gourmet Week', brandName: 'Maggi', transactionAmount: 2700, status: 'Paid' },
    { claimId: 'CLM-1019', campaignName: 'Health Drive', brandName: 'Horlicks', transactionAmount: 2400, status: 'Paid' },
  ],
}

const MOCK_AGENCIES: PendingAgency[] = [
  { id: 'a1', agencyName: 'Blinkit Agency', agencyInitials: 'BL', pendingClaimCount: 12, totalPendingAmount: 14800 },
  { id: 'a2', agencyName: 'Swiggy Partners', agencyInitials: 'SW', pendingClaimCount: 8, totalPendingAmount: 9600 },
  { id: 'a3', agencyName: 'Zepto Ads', agencyInitials: 'ZA', pendingClaimCount: 10, totalPendingAmount: 5200 },
  { id: 'a4', agencyName: 'Myntra Influence', agencyInitials: 'MI', pendingClaimCount: 6, totalPendingAmount: 4800 },
  { id: 'a5', agencyName: 'Amazon Connects', agencyInitials: 'AC', pendingClaimCount: 9, totalPendingAmount: 3600 },
  { id: 'a6', agencyName: 'Flipkart Ads', agencyInitials: 'FA', pendingClaimCount: 4, totalPendingAmount: 1400 },
  { id: 'a7', agencyName: 'Nykaa Creators', agencyInitials: 'NC', pendingClaimCount: 5, totalPendingAmount: 700 },
  { id: 'a8', agencyName: 'Meesho Network', agencyInitials: 'MN', pendingClaimCount: 3, totalPendingAmount: 500 },
]

const MOCK_PENDING_CLAIMS: Record<string, PendingClaim[]> = {
  a1: [
    { claimId: 'CLM-1087', campaignName: 'Diwali Push', submittedDate: 'Jul 20, 2026', expectedAmount: 1800, status: 'Under Review' },
    { claimId: 'CLM-1085', campaignName: 'Diwali Push', submittedDate: 'Jul 19, 2026', expectedAmount: 1200, status: 'Approved' },
    { claimId: 'CLM-1080', campaignName: 'Monsoon Fresh', submittedDate: 'Jul 15, 2026', expectedAmount: 900, status: 'Under Review' },
  ],
  a2: [
    { claimId: 'CLM-1076', campaignName: 'Gourmet Week', submittedDate: 'Jul 18, 2026', expectedAmount: 2100, status: 'Approved' },
    { claimId: 'CLM-1071', campaignName: 'Gourmet Week', submittedDate: 'Jul 12, 2026', expectedAmount: 1500, status: 'Under Review' },
  ],
}

function delay<T>(value: T, ms = 400): Promise<T> {
  return new Promise(resolve => setTimeout(() => resolve(value), ms))
}

export function fetchPaymentBatches(): Promise<PaymentBatch[]> {
  return delay(MOCK_BATCHES)
}

export function fetchPaymentClaims(batchId: string): Promise<PaymentClaim[]> {
  return delay(MOCK_CLAIMS[batchId] ?? [])
}

export function fetchPendingAgencies(): Promise<PendingAgency[]> {
  return delay(MOCK_AGENCIES)
}

export function fetchPendingClaims(agencyId: string): Promise<PendingClaim[]> {
  return delay(MOCK_PENDING_CLAIMS[agencyId] ?? [])
}
