import type { components } from '../types/api'
import type { ClaimReviewItem, ClaimScreenshotItem, ClaimStatus, ReviewStatus } from '../types/ClaimReviewTypes'
import type { Platform } from '../types/CampaignTypes'
import { fetchWithAuth, getAccessToken, throwIfUnauthorized } from './client'
import { rupeesToPaise } from '../utils/currency'
import { yyyymmddToIso } from '../utils/time'

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
    case 'CLAIM_REVIEW_STATUS_APPROVED':
      return 'approved'
    case 'CLAIM_REVIEW_STATUS_REJECTED':
      return 'rejected'
    case 'CLAIM_REVIEW_STATUS_PROOF_REQUESTED':
      return 'in-review'
    case 'CLAIM_REVIEW_STATUS_OBJECTED':
      return 'objected'
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
    orderDate: yyyymmddToIso(dto.orderDate ?? undefined),
    mediatorName: '',
    buyerName: '',
    claimStatus: toClaimStatus(status),
    reviewStatus: toReviewStatus(status),
    approvalMethod: 'manual',
    mediatorVerified: dto.mediatorVerified ?? false,
    matchPct: dto.score ?? 0,
    platform: (dto.platform ?? '') as Platform,
    brandName: '',
    accountName: dto.accountName ?? undefined,
    orderedBy: dto.orderedBy ?? undefined,
    productName: dto.productName ?? dto.deal?.productName ?? undefined,
    sellerName: dto.sellerName ?? undefined,
    amountPaise: dto.amountPaise ?? undefined,
    reviewUrl: dto.reviewUrl ?? undefined,
    currentStep: dto.currentStep ?? undefined,
    reviewerComments: dto.reviewerComments ?? undefined,
    isUnderReview: status === 'UNDER_REVIEW',
    screenshots: (dto.screenshots ?? []).map(s => ({
      id: s.id ?? '',
      storageKey: s.storageKey ?? '',
      type: s.type ?? '',
      score: s.score,
      extractedDetails: s.extractedDetails as ClaimScreenshotItem['extractedDetails'],
      verificationStatus: s.verificationStatus as ClaimScreenshotItem['verificationStatus'],
    })),
  }
}

function mapClaimReview(dto: ClaimReviewResponseDto): ClaimReviewItem {
  const backendStatus = (dto.claimStatus ?? 'CREATED') as BackendClaimStatus
  return {
    id: dto.claimId ?? dto.id ?? '',
    campaignId: dto.campaignId ?? '',
    campaignName: dto.campaignName ?? '',
    orderId: dto.ecommerceOrderId ?? '',
    orderDate: yyyymmddToIso(dto.orderDate),
    mediatorName: dto.dealOwnerName ?? '',
    buyerName: dto.buyerName ?? '',
    claimStatus: toClaimStatus(backendStatus),
    reviewStatus: reviewStatusFromClaimReviewStatus(
      dto.claimReviewStatus ?? 'CLAIM_REVIEW_STATUS_PENDING'
    ),
    approvalMethod: 'manual',
    mediatorVerified: dto.mediatorVerified ?? false,
    matchPct: dto.matchScore ?? 0,
    platform: (dto.platform ?? '') as Platform,
    brandName: dto.brandName ?? '',
    isUnderReview: backendStatus === 'UNDER_REVIEW',
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

export async function fetchScreenshotUrl(storageKey: string): Promise<string> {
  const res = await fetchWithAuth(`${API_BASE}/files?key=${encodeURIComponent(storageKey)}`)
  if (!res.ok) throw new Error('Failed to fetch screenshot')
  const blob = await res.blob()
  return URL.createObjectURL(blob)
}

export async function fetchClaimsToReview(page = 0, size = 50): Promise<ClaimReviewItem[]> {
  const res = await fetchWithAuth(`${API_BASE}/claims/review?page=${page}&size=${size}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({}),
  })
  const data = (await res.json()) as PageClaimReviewResponseDto
  return (data.content ?? []).map(mapClaimReview)
}

type ScoredValue = components['schemas']['ScoredValue']

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
  extractedDetails: Record<string, ScoredValue>
  overallScore?: number | null
}

export async function submitReturn(claimId: string, screenshot: File): Promise<ClaimResponseDto> {
  const formData = new FormData()
  formData.append('screenshot', screenshot)

  const token = getAccessToken()
  const res = await fetch(`${API_BASE}/claims/${claimId}/return`, {
    method: 'POST',
    body: formData,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })

  throwIfUnauthorized(res)

  if (!res.ok) {
    let message = 'Failed to submit return screenshot. Please try again.'
    try {
      const body = (await res.clone().json()) as Record<string, unknown>
      if (typeof body['message'] === 'string') message = body['message']
    } catch { /* ignore */ }
    throw new Error(message)
  }

  return (await res.json()) as ClaimResponseDto
}

export async function submitReview(claimId: string, screenshot: File, reviewUrl?: string): Promise<ClaimResponseDto> {
  const formData = new FormData()
  formData.append('screenshot', screenshot)
  if (reviewUrl) formData.append('reviewUrl', reviewUrl)

  const token = getAccessToken()
  const res = await fetch(`${API_BASE}/claims/${claimId}/review`, {
    method: 'POST',
    body: formData,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })

  throwIfUnauthorized(res)

  if (!res.ok) {
    let message = 'Failed to submit review. Please try again.'
    try {
      const body = (await res.clone().json()) as Record<string, unknown>
      if (typeof body['message'] === 'string') message = body['message']
    } catch { /* ignore */ }
    throw new Error(message)
  }

  return (await res.json()) as ClaimResponseDto
}

export async function submitRating(claimId: string, screenshot: File): Promise<ClaimResponseDto> {
  const formData = new FormData()
  formData.append('screenshot', screenshot)

  const token = getAccessToken()
  const res = await fetch(`${API_BASE}/claims/${claimId}/rating`, {
    method: 'POST',
    body: formData,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })

  throwIfUnauthorized(res)

  if (!res.ok) {
    let message = 'Failed to submit rating. Please try again.'
    try {
      const body = (await res.clone().json()) as Record<string, unknown>
      if (typeof body['message'] === 'string') message = body['message']
    } catch { /* ignore */ }
    throw new Error(message)
  }

  return (await res.json()) as ClaimResponseDto
}

export async function updateScreenshot(
  claimId: string,
  screenshotId: string,
  screenshotType: string,
  screenshot: File
): Promise<ClaimResponseDto> {
  const formData = new FormData()
  formData.append('screenshotId', screenshotId)
  formData.append('screenshotType', screenshotType)
  formData.append('screenshot', screenshot)

  const token = getAccessToken()
  const res = await fetch(`${API_BASE}/claims/${claimId}/update`, {
    method: 'POST',
    body: formData,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })

  throwIfUnauthorized(res)

  if (!res.ok) {
    let message = 'Failed to update screenshot. Please try again.'
    try {
      const body = (await res.clone().json()) as Record<string, unknown>
      if (typeof body['message'] === 'string') message = body['message']
    } catch { /* ignore */ }
    throw new Error(message)
  }

  return (await res.json()) as ClaimResponseDto
}

type ScreenshotVerificationAction = 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' | 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'

export async function reviewScreenshot(screenshotId: string, claimId: string, action: ScreenshotVerificationAction, reviewerComment?: string): Promise<ClaimReviewItem> {
  const res = await fetchWithAuth(`${API_BASE}/claims/screenshots/review`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ screenshotId, claimId, action, reviewerComments: reviewerComment }),
  })
  if (!res.ok) {
    let message = 'Failed to submit screenshot review.'
    try {
      const body = (await res.clone().json()) as Record<string, unknown>
      if (typeof body['message'] === 'string') message = body['message']
    } catch { /* ignore */ }
    throw new Error(message)
  }
  return mapClaim((await res.json()) as ClaimResponseDto)
}

export async function submitClaimReview(
  claimId: string,
  decision: 'APPROVED' | 'REJECTED' | 'VERIFIED',
  comment?: string
): Promise<ClaimReviewItem> {
  const res = await fetchWithAuth(`${API_BASE}/claims/${claimId}/submitReview`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ reviewerDecision: decision, reviewerComment: comment ?? '' }),
  })
  if (!res.ok) {
    let message = 'Failed to submit claim review.'
    try {
      const body = (await res.clone().json()) as Record<string, unknown>
      if (typeof body['message'] === 'string') message = body['message']
    } catch { /* ignore */ }
    throw new Error(message)
  }
  return mapClaim((await res.json()) as ClaimResponseDto)
}

export async function submitClaim(params: SubmitClaimParams): Promise<ClaimResponseDto> {
  const formData = new FormData()
  formData.append('campaignId', params.campaignId)
  formData.append('dealId', params.dealId)
  formData.append('platform', params.platform)
  formData.append('orderId', params.orderId)
  formData.append('amount', String(rupeesToPaise(params.amount)))
  formData.append('productName', params.productName)
  formData.append('sellerName', params.sellerName)
  // Convert YYYY-MM-DD to YYYYMMDD integer expected by the backend
  formData.append('orderDate', params.orderDate.replace(/-/g, ''))
  formData.append('accountName', params.accountName)
  formData.append('screenshot', params.screenshot)
  for (const [key, sv] of Object.entries(params.extractedDetails)) {
    formData.append(`extractedDetails[${key}]`, JSON.stringify(sv))
  }
  if (params.overallScore != null) {
    formData.append('overallScore', String(params.overallScore))
  }

  const token = getAccessToken()
  const res = await fetch(`${API_BASE}/claims`, {
    method: 'POST',
    body: formData,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })

  throwIfUnauthorized(res)

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
