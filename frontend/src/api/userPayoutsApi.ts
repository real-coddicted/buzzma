import { fetchWithAuth } from './client'
import { fetchUserById, fetchUserBanking } from './userApi'
import { fetchCampaignById } from './campaignApi'
import { paiseToRupees } from '../utils/currency'
import type { PayoutClaim, PayoutUser, PaymentSubmission, PaidPayee, MadePayment } from '../types/UserPayoutsTypes'
import { PAYMENT_METHODS } from '../types/UserPayoutsTypes'

export interface Paged<T> {
  items: T[]
  total: number
  totalPages: number
}

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

interface PaidPayoutDto {
  payeeId: string
  claimCount: number
  paymentCount: number
  totalAmountPaidPaise: number
  lastPaidAt: string
}

interface MadePaymentDto {
  paymentId: string
  claimCount: number
  totalAmountPaise: number
  paidAt: string
  paymentMethod: string | null
  screenshotStorageKey: string | null
}

function initials(name: string): string {
  return name.split(' ').slice(0, 2).map(w => w[0]?.toUpperCase() ?? '').join('')
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

function paymentMethodLabel(method: string | null): string {
  const m = PAYMENT_METHODS.find(pm => pm.value === (method ?? '').toLowerCase())
  return m?.label ?? method ?? '—'
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

export async function fetchPaidPayees(page: number, size: number): Promise<Paged<PaidPayee>> {
  const res = await fetchWithAuth(`/api/v1/payouts/paid?page=${page - 1}&size=${size}`)
  const { items: dtos, total, totalPages }: { items: PaidPayoutDto[]; total: number; totalPages: number } =
    await res.json()

  const items = await Promise.all(
    dtos.map(async d => {
      const user = await fetchUserById(d.payeeId).catch(() => null)
      const name = user?.name ?? d.payeeId.slice(0, 8)
      const role: 'Mediator' | 'Buyer' = user?.role === 'ROLE_MEDIATOR' ? 'Mediator' : 'Buyer'
      return {
        id: d.payeeId,
        name,
        initials: initials(name),
        role,
        claimCount: d.claimCount,
        paymentCount: d.paymentCount,
        totalAmount: paiseToRupees(d.totalAmountPaidPaise),
        lastPaidDate: formatDate(d.lastPaidAt),
      } satisfies PaidPayee
    }),
  )
  return { items, total, totalPages }
}

export async function fetchPaymentsForPayee(payeeId: string, page: number, size: number): Promise<Paged<MadePayment>> {
  const res = await fetchWithAuth(`/api/v1/payouts/${payeeId}/payments?page=${page - 1}&size=${size}`)
  const { items: dtos, total, totalPages }: { items: MadePaymentDto[]; total: number; totalPages: number } =
    await res.json()

  const items: MadePayment[] = dtos.map(d => ({
    id: d.paymentId,
    claimCount: d.claimCount,
    totalAmount: paiseToRupees(d.totalAmountPaise),
    paidAt: formatDate(d.paidAt),
    paymentMethod: paymentMethodLabel(d.paymentMethod),
    screenshotStorageKey: d.screenshotStorageKey ?? undefined,
  }))
  return { items, total, totalPages }
}

export async function fetchClaimsForPayment(paymentId: string, page: number, size: number): Promise<Paged<PayoutClaim>> {
  const res = await fetchWithAuth(`/api/v1/payouts/payments/${paymentId}/claims?page=${page - 1}&size=${size}`)
  const { items: dtos, total, totalPages }: { items: ClaimAccountingSummaryDto[]; total: number; totalPages: number } =
    await res.json()

  const cache = new Map<string, { title: string; brand: string; platform: string }>()
  const items = await enrichClaims(dtos, cache)
  return { items, total, totalPages }
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

  const formData = new FormData()
  formData.append('screenshot', submission.screenshot)
  formData.append(
    'request',
    new Blob(
      [JSON.stringify({
        paymentMethod: backendMethod,
        paidAt: new Date().toISOString(),
        utrRef: submission.utrRef ?? null,
        notes: submission.notes ?? null,
        claimIds: claimIds.length > 0 ? claimIds : null,
      })],
      { type: 'application/json' },
    ),
  )

  await fetchWithAuth(`/api/v1/payouts/${userId}/pay`, {
    method: 'POST',
    body: formData,
  })
}
