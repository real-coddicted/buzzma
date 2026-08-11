import { fetchWithAuth } from './client'
import { fetchUsersByIds, type UserBriefDto } from './userApi'
import { fetchCampaignsByIds } from './campaignApi'
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

  const usersById = new Map<string, UserBriefDto>(
    (await fetchUsersByIds(dtos.map(d => d.payerId))).map(u => [u.id, u]),
  )

  return dtos.map(d => ({
    id: d.paymentId,
    date: formatDate(d.paidAt),
    agencyName: usersById.get(d.payerId)?.name ?? d.payerId.slice(0, 8),
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
  const campaigns = await fetchCampaignsByIds(uniqueCampaignIds)
  const campaignMap = new Map(campaigns.map(c => [c.id, c]))

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

  const usersById = new Map<string, UserBriefDto>(
    (await fetchUsersByIds(dtos.map(d => d.counterpartyId))).map(u => [u.id, u]),
  )

  return dtos.map(d => {
    const name = usersById.get(d.counterpartyId)?.name ?? d.counterpartyId.slice(0, 8)
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
  const campaigns = await fetchCampaignsByIds(uniqueCampaignIds)
  const campaignMap = new Map(campaigns.map(c => [c.id, c.title ?? c.id.slice(0, 8)]))

  return dtos.map(d => ({
    claimId: d.claimId,
    campaignName: campaignMap.get(d.campaignId) ?? d.campaignId.slice(0, 8),
    submittedDate: formatDate(d.createdAt),
    expectedAmount: paiseToRupees(d.amountPaise),
    status: 'Pending',
  }))
}
