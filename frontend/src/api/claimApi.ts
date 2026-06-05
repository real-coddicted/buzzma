import type { components } from '../types/api'
import type { ClaimReviewItem, ClaimStatus, ReviewStatus } from '../types/ClaimReviewTypes'
import { fetchWithAuth, getAccessToken, clearSession } from './client'

const API_BASE = '/api/v1'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type BackendClaimStatus = NonNullable<ClaimResponseDto['status']>
type ClaimReviewResponseDto = components['schemas']['ClaimReviewResponseDto']
type ClaimReviewStatus = NonNullable<ClaimReviewResponseDto['claimReviewStatus']>
type PageClaimReviewResponseDto = components['schemas']['PageClaimReviewResponseDto']

function toReviewStatus(status: BackendClaimStatus): ReviewStatus {
  switch (status) {
    case 'APPROVED':
    case 'REWARD_PENDING':
    case 'COMPLETED':
      return 'approved'
    case 'REJECTED':
    case 'FAILED':
      return 'rejected'
    case 'UNDER_REVIEW':
    case 'ADDITIONAL_PROOF_REQUESTED':
      return 'in-review'
    default:
      return 'pending'
  }
}

function toClaimStatus(status: BackendClaimStatus): ClaimStatus {
  return status === 'COMPLETED' || status === 'REWARD_PENDING' ? 'completed' : 'in-progress'
}

function reviewStatusFromClaimReviewStatus(status: ClaimReviewStatus): ReviewStatus {
  switch (status) {
    case 'CLAIM_REVIEW_STATUS_ACCEPTED':
      return 'approved'
    case 'CLAIM_REVIEW_STATUS_REJECTED':
      return 'rejected'
    case 'CLAIM_REVIEW_STATUS_PROOF_REQUESTED':
      return 'in-review'
    default:
      return 'pending'
  }
}

function mapClaim(dto: ClaimResponseDto): ClaimReviewItem {
  const status = dto.status ?? 'CREATED'
  return {
    id: dto.id ?? '',
    campaignId: '',
    campaignName: dto.productName ?? dto.deal?.productName ?? '',
    orderId: dto.ecommerceOrderId ?? '',
    orderDate: dto.orderDate != null ? String(dto.orderDate) : '',
    mediatorName: '',
    claimStatus: toClaimStatus(status),
    reviewStatus: toReviewStatus(status),
    approvalMethod: 'manual',
    mediatorVerified: dto.mediatorVerified ?? false,
    matchPct: Math.round((dto.score ?? 0) * 100),
    accountName: dto.accountName ?? undefined,
    productName: dto.productName ?? dto.deal?.productName ?? undefined,
    sellerName: dto.sellerName ?? undefined,
    amountPaise: dto.amountPaise ?? undefined,
    reviewUrl: dto.reviewUrl ?? undefined,
    currentStep: dto.currentStep ?? undefined,
    reviewerComments: dto.reviewerComments ?? undefined,
  }
}

function mapClaimReview(dto: ClaimReviewResponseDto): ClaimReviewItem {
  const backendStatus = (dto.claimStatus ?? 'CREATED') as BackendClaimStatus
  return {
    id: dto.claimId ?? dto.id ?? '',
    campaignId: dto.campaignId ?? '',
    campaignName: dto.campaignName ?? '',
    orderId: dto.ecommerceOrderId ?? '',
    orderDate: dto.createdAt ? dto.createdAt.slice(0, 10) : '',
    mediatorName: dto.dealOwnerName ?? '',
    claimStatus: toClaimStatus(backendStatus),
    reviewStatus: reviewStatusFromClaimReviewStatus(
      dto.claimReviewStatus ?? 'CLAIM_REVIEW_STATUS_PENDING'
    ),
    approvalMethod: 'manual',
    mediatorVerified: dto.mediatorVerified ?? false,
    matchPct: Math.round((dto.matchScore ?? 0) * 100),
  }
}

export async function fetchClaims(): Promise<ClaimReviewItem[]> {
  const res = await fetchWithAuth(`${API_BASE}/claims`)
  const data = (await res.json()) as ClaimResponseDto[]
  return data.map(mapClaim)
}

export async function fetchRawClaims(): Promise<ClaimResponseDto[]> {
  const res = await fetchWithAuth(`${API_BASE}/claims`)
  return (await res.json()) as ClaimResponseDto[]
}

export async function fetchClaimById(id: string): Promise<ClaimReviewItem> {
  const res = await fetchWithAuth(`${API_BASE}/claims/${id}`)
  const data = (await res.json()) as ClaimResponseDto
  return mapClaim(data)
}

export async function fetchClaimsToReview(page = 0, size = 50): Promise<ClaimReviewItem[]> {
  const res = await fetchWithAuth(`${API_BASE}/claims/review?page=${page}&size=${size}`)
  const data = (await res.json()) as PageClaimReviewResponseDto
  return (data.content ?? []).map(mapClaimReview)
}

export interface SubmitClaimParams {
  campaignId: string
  dealId: string
  platform: string
  orderId: string
  amount: number
  productName: string
  sellerName: string
  orderDate: string   // YYYY-MM-DD from date picker
  accountName: string
  screenshot: File
  extractedDetails: Record<string, string>
}

export async function submitClaim(params: SubmitClaimParams): Promise<ClaimResponseDto> {
  const formData = new FormData()
  formData.append('campaignId', params.campaignId)
  formData.append('dealId', params.dealId)
  formData.append('platform', params.platform)
  formData.append('orderId', params.orderId)
  formData.append('amount', String(Math.round(params.amount)))
  formData.append('productName', params.productName)
  formData.append('sellerName', params.sellerName)
  // Convert YYYY-MM-DD to YYYYMMDD integer expected by the backend
  formData.append('orderDate', params.orderDate.replace(/-/g, ''))
  formData.append('accountName', params.accountName)
  formData.append('screenshot', params.screenshot)
  for (const [key, value] of Object.entries(params.extractedDetails)) {
    formData.append(`extractedDetails[${key}]`, value)
  }

  const token = getAccessToken()
  const res = await fetch(`${API_BASE}/claims`, {
    method: 'POST',
    body: formData,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })

  if (res.status === 401) {
    clearSession()
    window.dispatchEvent(new CustomEvent('auth:logout'))
    throw new Error('Session expired. Please sign in again.')
  }

  if (!res.ok) {
    let message = 'Failed to submit claim. Please try again.'
    try {
      const body = (await res.clone().json()) as Record<string, unknown>
      if (typeof body['message'] === 'string') message = body['message']
    } catch { /* ignore */ }
    throw new Error(message)
  }

  return (await res.json()) as ClaimResponseDto
}
