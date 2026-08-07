import { fetchWithAuth } from './client'
import { fetchUserById, fetchUserBanking } from './userApi'
import { fetchCampaignById } from './campaignApi'
import { paiseToRupees } from '../utils/currency'
import type { PayoutClaim, PayoutUser, PaymentSubmission } from '../types/UserPayoutsTypes'
import { PAYMENT_METHODS } from '../types/UserPayoutsTypes'

interface PendingPayoutDto {
  payeeId: string
  claimCount: number
  totalAmountPaise: number
  oldestClaimAt: string
}

interface ClaimAccountingSummaryDto {
  id: string
  claimId: string
  campaignId: string
  dealId: string
  amountPaise: number
  createdAt: string
}

function initials(name: string): string {
  return name.split(' ').slice(0, 2).map(w => w[0]?.toUpperCase() ?? '').join('')
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

async function fetchClaimsForPayee(payeeId: string): Promise<ClaimAccountingSummaryDto[]> {
  const res = await fetchWithAuth(`/api/v1/payouts/${payeeId}/claims`)
  return res.json() as Promise<ClaimAccountingSummaryDto[]>
}

async function enrichClaims(
  dtos: ClaimAccountingSummaryDto[],
  campaignCache: Map<string, { title: string; brand: string; platform: string }>,
): Promise<PayoutClaim[]> {
  const uncached = [...new Set(dtos.map(d => d.campaignId).filter(id => !campaignCache.has(id)))]
  await Promise.all(
    uncached.map(id =>
      fetchCampaignById(id)
        .then(c => campaignCache.set(id, { title: c.title ?? id.slice(0, 8), brand: c.productBrandName ?? '—', platform: c.platform ?? '' }))
        .catch(() => campaignCache.set(id, { title: id.slice(0, 8), brand: '—', platform: '' })),
    ),
  )
  return dtos.map(d => ({
    id: d.id,
    campaign: campaignCache.get(d.campaignId)?.title ?? d.campaignId.slice(0, 8),
    brand: campaignCache.get(d.campaignId)?.brand ?? '—',
    platform: campaignCache.get(d.campaignId)?.platform ?? '',
    approvedDate: formatDate(d.createdAt),
    amount: paiseToRupees(d.amountPaise),
  }))
}

export async function fetchPayoutUsers(): Promise<PayoutUser[]> {
  const res = await fetchWithAuth('/api/v1/payouts/pending')
  const dtos: PendingPayoutDto[] = await res.json()

  return Promise.all(
    dtos.map(async d => {
      const [user, banking] = await Promise.all([
        fetchUserById(d.payeeId).catch(() => null),
        fetchUserBanking(d.payeeId).catch(() => null),
      ])
      const name = user?.name ?? d.payeeId.slice(0, 8)
      const role: 'Mediator' | 'Buyer' = user?.role === 'ROLE_MEDIATOR' ? 'Mediator' : 'Buyer'
      return {
        id: d.payeeId,
        name,
        initials: initials(name),
        role,
        upiId: banking?.upiId ?? '—',
        oldestClaimDate: formatDate(d.oldestClaimAt),
        claimCount: d.claimCount,
        totalAmount: paiseToRupees(d.totalAmountPaise),
      } satisfies PayoutUser
    }),
  )
}

export async function fetchPayoutClaims(userId: string): Promise<PayoutClaim[]> {
  const dtos = await fetchClaimsForPayee(userId)
  const cache = new Map<string, { title: string; brand: string; platform: string }>()
  return enrichClaims(dtos, cache)
}

export async function submitPayment(
  userId: string,
  claimIds: string[],
  submission: PaymentSubmission,
): Promise<void> {
  const knownValues = new Set<string>(PAYMENT_METHODS.map(pm => pm.value))
  const backendMethod = knownValues.has(submission.paymentMethod)
    ? submission.paymentMethod.toUpperCase()
    : 'OTHER'

  await fetchWithAuth(`/api/v1/payouts/${userId}/pay`, {
    method: 'POST',
    body: JSON.stringify({
      paymentMethod: backendMethod,
      paidAt: new Date().toISOString(),
      utrRef: submission.utrRef ?? null,
      notes: submission.notes ?? null,
      claimIds: claimIds.length > 0 ? claimIds : null,
    }),
  })
}
