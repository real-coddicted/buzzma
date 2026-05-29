import type { components } from '../types/api'
import type { Campaign, CampaignRequestDto, CampaignStatus, CampaignType, Platform } from '../types'
import { fetchWithAuth, getCurrentUser } from './client'

const API_BASE = '/api/v1'

type BackendRequest = components['schemas']['CampaignRequestDto']
export type CampaignResponseDto = components['schemas']['CampaignResponseDto']
type CampaignSummaryDto = components['schemas']['CampaignSummaryResponseDto']

const statusMap: Record<NonNullable<CampaignSummaryDto['status']>, CampaignStatus> = {
  CAMPAIGN_STATUS_DRAFT:     'draft',
  CAMPAIGN_STATUS_ACTIVE:    'active',
  CAMPAIGN_STATUS_ASSIGNED:  'active',
  CAMPAIGN_STATUS_PAUSED:    'paused',
  CAMPAIGN_STATUS_COMPLETED: 'completed',
  CAMPAIGN_STATUS_CLOSED:    'completed',
}

export function yyyymmddToIso(n: number | undefined): string {
  if (!n) return ''
  const s = n.toString().padStart(8, '0')
  return `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)}`
}

function isoToYYYYMMDD(iso: string): number {
  return parseInt(iso.replace(/-/g, ''), 10)
}

export async function createCampaign(dto: CampaignRequestDto): Promise<CampaignResponseDto> {
  const user = getCurrentUser()
  if (!user?.id) throw new Error('You must be signed in to create a campaign.')

  const body: BackendRequest = {
    title: dto.title,
    ownerId: user.id,
    platform: dto.platform as BackendRequest['platform'],
    productName: dto.productName,
    productBrandName: dto.productBrandName,
    productImageUrl: dto.productImageUrl,
    productUrl: dto.productUrl,
    originalPricePaise: dto.originalPricePaise,
    campaignPricePaise: dto.campaignPricePaise,
    campaignType: (dto.campaignType ?? 'CAMPAIGN_TYPE_ORDER') as BackendRequest['campaignType'],
    campaignStatus: 'CAMPAIGN_STATUS_DRAFT',
    totalSlots: dto.totalSlots ?? 1,
    openToAll: dto.openToAll ?? true,
    ...(dto.returnWindowDays != null ? { returnWindowDays: dto.returnWindowDays } : {}),
    ...(dto.termsAndConditions ? { termsAndConditions: dto.termsAndConditions } : {}),
    ...(dto.sellerName ? { sellerName: dto.sellerName } : {}),
    ...(dto.startDate ? { startDate: isoToYYYYMMDD(dto.startDate) } : {}),
    ...(dto.endDate ? { endDate: isoToYYYYMMDD(dto.endDate) } : {}),
    ...(dto.assignees && dto.assignees.length > 0 ? {
      assignees: dto.assignees.map(e => ({
        campaignId: '',
        assignorId: user.id!,
        assigneeId: e.id,
        campaignPricePaise: dto.campaignPricePaise,
        commissionOfferedPaise: Math.round(e.commissionOffered * 100),
        slotOffered: e.slotsAvailable,
      })),
    } : {}),
  }

  const res = await fetchWithAuth(`${API_BASE}/campaigns`, {
    method: 'POST',
    body: JSON.stringify(body),
  })

  return res.json() as Promise<CampaignResponseDto>
}

export async function fetchCampaigns(): Promise<Campaign[]> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns`)
  const data = (await res.json()) as CampaignSummaryDto[]
  return data.map(dto => ({
    id:                   dto.campaignId ?? '',
    title:                dto.title ?? '',
    status:               statusMap[dto.status ?? 'CAMPAIGN_STATUS_DRAFT'],
    platform:             (dto.platform ?? 'PLATFORM_AMAZON') as Platform,
    productBrandName:     '',
    productName:          dto.productName ?? '',
    productImageUrl:      dto.productImageUrl ?? '',
    productUrl:           '',
    originalPricePaise:   0,
    campaignPricePaise:   0,
    commissionOfferedPaise: 0,
    returnWindowDays:     null,
    campaignType:         (dto.type ?? null) as CampaignType | null,
    totalSlots:           dto.totalSlots ?? null,
    slotsClaimed:         dto.slotsClaimed ?? 0,
    allowedAgencies:      null,
    openToAll:            true,
    budget:               Math.round((dto.budgetPaise ?? 0) / 100),
    spent:                0,
    impressions:          0,
    clicks:               0,
    conversions:          0,
    ctr:                  0,
    startDate:            yyyymmddToIso(dto.startDate),
    endDate:              yyyymmddToIso(dto.endDate),
  }))
}

export async function fetchCampaignById(id: string): Promise<CampaignResponseDto> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/${id}`)
  return res.json() as Promise<CampaignResponseDto>
}

export async function copyCampaign(id: string): Promise<CampaignResponseDto> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/${id}/copy`, { method: 'POST' })
  return res.json() as Promise<CampaignResponseDto>
}

export async function updateCampaign(
  id: string,
  dto: CampaignRequestDto,
  campaignStatus: NonNullable<CampaignResponseDto['status']>,
): Promise<CampaignResponseDto> {
  const user = getCurrentUser()
  if (!user?.id) throw new Error('You must be signed in to update a campaign.')

  const body: BackendRequest = {
    title: dto.title,
    ownerId: user.id,
    platform: dto.platform as BackendRequest['platform'],
    productName: dto.productName,
    productBrandName: dto.productBrandName,
    productImageUrl: dto.productImageUrl,
    productUrl: dto.productUrl,
    originalPricePaise: dto.originalPricePaise,
    campaignPricePaise: dto.campaignPricePaise,
    campaignType: (dto.campaignType ?? 'CAMPAIGN_TYPE_ORDER') as BackendRequest['campaignType'],
    campaignStatus,
    totalSlots: dto.totalSlots ?? 1,
    openToAll: dto.openToAll ?? true,
    ...(dto.returnWindowDays != null ? { returnWindowDays: dto.returnWindowDays } : {}),
    ...(dto.termsAndConditions ? { termsAndConditions: dto.termsAndConditions } : {}),
    ...(dto.sellerName ? { sellerName: dto.sellerName } : {}),
    ...(dto.startDate ? { startDate: isoToYYYYMMDD(dto.startDate) } : {}),
    ...(dto.endDate ? { endDate: isoToYYYYMMDD(dto.endDate) } : {}),
    ...(dto.assignees && dto.assignees.length > 0 ? {
      assignees: dto.assignees.map(e => ({
        campaignId: id,
        assignorId: user.id!,
        assigneeId: e.id,
        campaignPricePaise: dto.campaignPricePaise,
        commissionOfferedPaise: Math.round(e.commissionOffered * 100),
        slotOffered: e.slotsAvailable,
      })),
    } : {}),
  }

  const res = await fetchWithAuth(`${API_BASE}/campaigns/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(body),
  })

  return res.json() as Promise<CampaignResponseDto>
}

export async function publishCampaign(campaignId: string): Promise<CampaignResponseDto> {
  const res = await fetchWithAuth(
    `${API_BASE}/campaigns/${campaignId}/action/CAMPAIGN_ACTION_PUBLISH`,
    { method: 'POST' },
  )
  return res.json() as Promise<CampaignResponseDto>
}
