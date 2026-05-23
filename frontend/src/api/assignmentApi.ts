import type { components } from '../types/api'
import type { AssignmentItem } from '../types/AssignmentTypes'
import { fetchWithAuth } from './client'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../constants/campaigns'

const API_BASE = '/api/v1'

type AssignmentResponseDto = components['schemas']['AssignmentResponseDto']
type PublishAssignmentRequestDto = components['schemas']['PublishAssignmentRequestDto']

function mapAssignment(dto: AssignmentResponseDto): AssignmentItem {
  const platform = dto.platform ?? 'PLATFORM_AMAZON'
  const dealType = (dto.dealType ?? 'CAMPAIGN_TYPE_ORDER') as AssignmentItem['dealType']
  return {
    id: dto.id ?? '',
    campaignId: dto.campaignId ?? '',
    productName: dto.productName ?? '',
    productImageUrl: dto.productImageUrl ?? '',
    productUrl: dto.productUrl ?? '',
    platform,
    platformLabel: PLATFORM_LABELS[platform] ?? platform,
    dealType,
    dealTypeLabel: CAMPAIGN_TYPE_LABELS[dealType] ?? dealType,
    originalPricePaise: dto.originalPricePaise ?? 0,
    offeredPricePaise: dto.offeredPricePaise ?? 0,
    commissionOfferedPaise: dto.commissionOfferedPaise ?? 0,
    slotsOffered: dto.slotLimit ?? 0,
    sellerName: dto.sellerName,
    termsAndConditions: dto.termsAndConditions,
  }
}

export async function fetchUnpublishedAssignments(): Promise<AssignmentItem[]> {
  const res = await fetchWithAuth(
    `${API_BASE}/assignments?status=CAMPAIGN_ASSIGNMENT_STATUS_DRAFT`,
  )
  const data = (await res.json()) as components['schemas']['PagedAssignmentsResponseDto']
  return (data.items ?? []).map(mapAssignment)
}

export async function fetchPublishedAssignments(): Promise<AssignmentItem[]> {
  const res = await fetchWithAuth(
    `${API_BASE}/assignments?status=CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED`,
  )
  const data = (await res.json()) as components['schemas']['PagedAssignmentsResponseDto']
  return (data.items ?? []).map(mapAssignment)
}

export async function fetchCommissionCharged(campaignId: string): Promise<number> {
  const res = await fetchWithAuth(
    `${API_BASE}/assignments/commissionCharged/${campaignId}`,
  )
  const data = (await res.json()) as components['schemas']['CommissionResponseDto']
  return data.commissionPaise ?? 0
}

export async function publishAssignment(
  assignmentId: string,
  campaignId: string,
  commissionChargedPaise: number,
  dealPricePaise: number,
): Promise<boolean> {
  const body: PublishAssignmentRequestDto = {
    campaignId,
    commissionChargedPaise,
    dealPricePaise,
  }
  const res = await fetchWithAuth(`${API_BASE}/assignments/${assignmentId}/publish`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return (await res.json()) as boolean
}

