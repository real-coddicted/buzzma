import type { PayoutClaim, PayoutUser, PaymentSubmission } from '../types/UserPayoutsTypes'

const MOCK_USERS: PayoutUser[] = [
  { id: 'u1', name: 'Rahul Mehta',  initials: 'RM', role: 'Mediator', upiId: 'rahul@oksbi',   oldestClaimDate: '14 Jun 2026' },
  { id: 'u2', name: 'Priya Sharma', initials: 'PS', role: 'Mediator', upiId: 'priya@kotak',   oldestClaimDate: '21 Jun 2026' },
  { id: 'u3', name: 'Amit Verma',   initials: 'AV', role: 'Buyer',    upiId: 'amit@ybl',      oldestClaimDate: '28 Jun 2026' },
  { id: 'u4', name: 'Sunita Patel', initials: 'SP', role: 'Mediator', upiId: 'sunita@paytm',  oldestClaimDate: '02 Jul 2026' },
  { id: 'u5', name: 'Deepak Kumar', initials: 'DK', role: 'Buyer',    upiId: 'deepak@upi',    oldestClaimDate: '08 Jul 2026' },
  { id: 'u6', name: 'Kavitha Nair', initials: 'KN', role: 'Mediator', upiId: 'kavitha@oksbi', oldestClaimDate: '11 Jul 2026' },
]

const MOCK_CLAIMS: Record<string, PayoutClaim[]> = {
  u1: [
    { id: 'CLM-4501', campaign: 'Amazon Summer Sale',   brand: 'Amazon',   approvedDate: '14 Jun 2026', amount: 1800 },
    { id: 'CLM-4523', campaign: 'Flipkart Big Days',    brand: 'Flipkart', approvedDate: '20 Jun 2026', amount: 1200 },
    { id: 'CLM-4587', campaign: 'Amazon Prime Day',     brand: 'Amazon',   approvedDate: '25 Jun 2026', amount: 1250 },
  ],
  u2: [
    { id: 'CLM-4610', campaign: 'Meesho Mega Sale',     brand: 'Meesho',   approvedDate: '21 Jun 2026', amount: 1400 },
    { id: 'CLM-4634', campaign: 'Amazon Great Indian',  brand: 'Amazon',   approvedDate: '24 Jun 2026', amount: 1800 },
    { id: 'CLM-4689', campaign: 'Myntra End of Reason', brand: 'Myntra',   approvedDate: '28 Jun 2026', amount: 1600 },
    { id: 'CLM-4712', campaign: 'Flipkart BBD',         brand: 'Flipkart', approvedDate: '01 Jul 2026', amount: 1500 },
    { id: 'CLM-4745', campaign: 'Nykaa Sale',           brand: 'Nykaa',    approvedDate: '05 Jul 2026', amount: 1500 },
  ],
  u3: [
    { id: 'CLM-4820', campaign: 'Amazon Wardrobe',      brand: 'Amazon',   approvedDate: '28 Jun 2026', amount: 950  },
    { id: 'CLM-4856', campaign: 'Ajio Finale Sale',     brand: 'Ajio',     approvedDate: '04 Jul 2026', amount: 1000 },
  ],
  u4: [
    { id: 'CLM-4901', campaign: 'Amazon Sale',          brand: 'Amazon',   approvedDate: '02 Jul 2026', amount: 1400 },
    { id: 'CLM-4923', campaign: 'Flipkart Mega',        brand: 'Flipkart', approvedDate: '07 Jul 2026', amount: 1400 },
    { id: 'CLM-4967', campaign: 'Meesho Fashion',       brand: 'Meesho',   approvedDate: '10 Jul 2026', amount: 1400 },
    { id: 'CLM-4998', campaign: 'Myntra Sale',          brand: 'Myntra',   approvedDate: '14 Jul 2026', amount: 1400 },
  ],
  u5: [
    { id: 'CLM-5041', campaign: 'Amazon Back to School', brand: 'Amazon',  approvedDate: '08 Jul 2026', amount: 2100 },
  ],
  u6: [
    { id: 'CLM-5102', campaign: 'Flipkart Electronics', brand: 'Flipkart', approvedDate: '11 Jul 2026', amount: 1200 },
    { id: 'CLM-5134', campaign: 'Amazon Appliances',    brand: 'Amazon',   approvedDate: '16 Jul 2026', amount: 1100 },
    { id: 'CLM-5167', campaign: 'Nykaa Beauty',        brand: 'Nykaa',    approvedDate: '19 Jul 2026', amount: 1100 },
  ],
}

function delay<T>(value: T, ms = 400): Promise<T> {
  return new Promise(resolve => setTimeout(() => resolve(value), ms))
}

export function fetchPayoutUsers(): Promise<PayoutUser[]> {
  return delay(MOCK_USERS)
}

export function fetchPayoutClaims(userId: string): Promise<PayoutClaim[]> {
  return delay(MOCK_CLAIMS[userId] ?? [])
}

export function fetchAllPayoutClaims(): Promise<Record<string, PayoutClaim[]>> {
  return delay({ ...MOCK_CLAIMS })
}

export function submitPayment(
  _userId: string,
  _claimIds: string[],
  _submission: PaymentSubmission,
): Promise<void> {
  return delay(undefined as void, 600)
}
