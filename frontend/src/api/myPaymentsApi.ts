import { fetchWithAuth } from './client'
import { fetchUserById } from './userApi'
import { fetchCampaignById } from './campaignApi'
import { paiseToRupees } from '../utils/currency'
import { PAYMENT_METHODS } from '../types/UserPayoutsTypes'
import type { PaymentBatch, PaymentClaim, PendingAgency, PendingClaim } from '../types/MyPaymentsTypes'

interface ReceivedPaymentDto {
  paymentId: string
  payerId: string
  claimCount: number
  totalAmountPaise: number
  paidAt: string
  paymentMethod: string | null
  screenshotStorageKey: string | null
}

interface AwaitedPaymentDto {
  counterpartyId: string
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

function paymentMethodLabel(method: string | null): string {
  const m = PAYMENT_METHODS.find(pm => pm.value === (method ?? '').toLowerCase())
  return m?.label ?? method ?? '—'
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })
}

function initials(name: string): string {
  return name.split(' ').slice(0, 2).map(w => w[0]?.toUpperCase() ?? '').join('')
}

export async function fetchPaymentBatches(): Promise<PaymentBatch[]> {
  const res = await fetchWithAuth('/api/v1/my-payments/received')
  const dtos: ReceivedPaymentDto[] = await res.json()

  const uniquePayerIds = [...new Set(dtos.map(d => d.payerId))]
  const users = await Promise.all(uniquePayerIds.map(id => fetchUserById(id).catch(() => null)))
  const userMap = new Map(uniquePayerIds.map((id, i) => [id, users[i]]))

  return dtos.map(d => ({
    id: d.paymentId,
    date: formatDate(d.paidAt),
    agencyName: userMap.get(d.payerId)?.name ?? d.payerId.slice(0, 8),
    paymentMode: paymentMethodLabel(d.paymentMethod),
    totalAmount: paiseToRupees(d.totalAmountPaise),
    claimCount: d.claimCount,
    proofStorageKey: d.screenshotStorageKey ?? undefined,
  }))
}

export async function fetchPaymentClaims(paymentId: string): Promise<PaymentClaim[]> {
  const res = await fetchWithAuth(`/api/v1/payments/${paymentId}/claims`)
  const dtos: ClaimAccountingSummaryDto[] = await res.json()

  const uniqueCampaignIds = [...new Set(dtos.map(d => d.campaignId))]
  const campaigns = await Promise.all(uniqueCampaignIds.map(id => fetchCampaignById(id).catch(() => null)))
  const campaignMap = new Map(uniqueCampaignIds.map((id, i) => [id, campaigns[i]]))

  return dtos.map(d => {
    const campaign = campaignMap.get(d.campaignId)
    return {
      claimId: d.claimId,
      campaignName: campaign?.title ?? d.campaignId.slice(0, 8),
      brandName: campaign?.productBrandName ?? '—',
      transactionAmount: paiseToRupees(d.amountPaise),
      status: 'Paid',
    }
  })
}

export async function fetchPendingAgencies(): Promise<PendingAgency[]> {
  const res = await fetchWithAuth('/api/v1/my-payments/awaited')
  const dtos: AwaitedPaymentDto[] = await res.json()

  const uniqueIds = [...new Set(dtos.map(d => d.counterpartyId))]
  const users = await Promise.all(uniqueIds.map(id => fetchUserById(id).catch(() => null)))
  const userMap = new Map(uniqueIds.map((id, i) => [id, users[i]]))

  return dtos.map(d => {
    const name = userMap.get(d.counterpartyId)?.name ?? d.counterpartyId.slice(0, 8)
    return {
      id: d.counterpartyId,
      agencyName: name,
      agencyInitials: initials(name),
      pendingClaimCount: d.claimCount,
      totalPendingAmount: paiseToRupees(d.totalAmountPaise),
    }
  })
}

export async function fetchPendingClaims(agencyId: string): Promise<PendingClaim[]> {
  const res = await fetchWithAuth(`/api/v1/my-payments/awaited/${agencyId}/claims`)
  const dtos: ClaimAccountingSummaryDto[] = await res.json()

  const uniqueCampaignIds = [...new Set(dtos.map(d => d.campaignId))]
  const campaignNames = await Promise.all(
    uniqueCampaignIds.map(id =>
      fetchCampaignById(id).then(c => c.title ?? id.slice(0, 8)).catch(() => id.slice(0, 8))
    )
  )
  const campaignMap = new Map(uniqueCampaignIds.map((id, i) => [id, campaignNames[i]]))

  return dtos.map(d => ({
    claimId: d.claimId,
    campaignName: campaignMap.get(d.campaignId) ?? d.campaignId.slice(0, 8),
    submittedDate: formatDate(d.createdAt),
    expectedAmount: paiseToRupees(d.amountPaise),
    status: 'Pending',
  }))
}
