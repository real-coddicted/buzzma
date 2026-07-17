import type { components } from '../types/api'
import type { AssignmentItem, AssignmentSummary } from '../types/AssignmentTypes'
import { fetchWithAuth } from './client'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../constants/campaigns'
import { campaignStatusMap, type RawCampaignStatus } from '../utils/campaignStatus'

const API_BASE = '/api/v1'

type AssignmentResponseDto = components['schemas']['AssignmentResponseDto']
type AssignmentSummaryResponseDto = components['schemas']['AssignmentSummaryResponseDto']
type PublishAssignmentRequestDto = components['schemas']['PublishAssignmentRequestDto']
type AssignmentSummaryWithStatus = AssignmentSummaryResponseDto & { campaignStatus?: RawCampaignStatus }

function mapSummary(dto: AssignmentSummaryWithStatus): AssignmentSummary {
  const platform = dto.platform ?? 'PLATFORM_AMAZON'
  const dealType = (dto.dealType ?? 'CAMPAIGN_TYPE_ORDER') as AssignmentSummary['dealType']
  return {
    id: dto.id ?? '',
    productName: dto.productName ?? '',
    productImageUrl: dto.productImageUrl ?? '',
    platform,
    platformLabel: PLATFORM_LABELS[platform] ?? platform,
    dealType,
    dealTypeLabel: CAMPAIGN_TYPE_LABELS[dealType] ?? dealType,
    campaignStatus: campaignStatusMap[dto.campaignStatus ?? 'CAMPAIGN_STATUS_DRAFT'],
    originalPricePaise: dto.originalPricePaise ?? 0,
    offeredPricePaise: dto.offeredPricePaise ?? 0,
    slotsOffered: dto.slotLimit ?? 0,
  }
}

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
    campaignStatus: campaignStatusMap[dto.campaignStatus ?? 'CAMPAIGN_STATUS_DRAFT'],
    originalPricePaise: dto.originalPricePaise ?? 0,
    offeredPricePaise: dto.offeredPricePaise ?? 0,
    commissionOfferedPaise: dto.commissionOfferedPaise ?? 0,
    slotsOffered: dto.slotLimit ?? 0,
    sellerName: dto.sellerName,
    termsAndConditions: dto.termsAndConditions,
    affiliateLinkAllowed: dto.affiliateLinkAllowed ?? false,
  }
}

export interface PagedAssignments {
  items: AssignmentSummary[]
  total: number
  totalPages: number
}

export async function fetchUnpublishedAssignments(page = 0, size = 5): Promise<PagedAssignments> {
  const res = await fetchWithAuth(
    `${API_BASE}/assignments?status=CAMPAIGN_ASSIGNMENT_STATUS_LOCKED&page=${page}&size=${size}`,
  )
  const data = (await res.json()) as components['schemas']['PagedAssignmentsResponseDto']
  return {
    items:      (data.items ?? []).map(mapSummary),
    total:      data.total ?? 0,
    totalPages: data.totalPages ?? 0,
  }
}

export async function fetchPublishedAssignments(page = 0, size = 5): Promise<PagedAssignments> {
  const res = await fetchWithAuth(
    `${API_BASE}/assignments?status=CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED&page=${page}&size=${size}`,
  )
  const data = (await res.json()) as components['schemas']['PagedAssignmentsResponseDto']
  return {
    items:      (data.items ?? []).map(mapSummary),
    total:      data.total ?? 0,
    totalPages: data.totalPages ?? 0,
  }
}

export async function getAssignmentById(id: string): Promise<AssignmentItem> {
  const res = await fetchWithAuth(`${API_BASE}/assignments/${id}`)
  return mapAssignment((await res.json()) as AssignmentResponseDto)
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
  affiliateUrl?: string,
): Promise<boolean> {
  const body: PublishAssignmentRequestDto = {
    campaignId,
    commissionChargedPaise,
    dealPricePaise,
    ...(affiliateUrl ? { affiliateUrl } : {}),
  }
  const res = await fetchWithAuth(`${API_BASE}/assignments/${assignmentId}/publish`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return (await res.json()) as boolean
}

