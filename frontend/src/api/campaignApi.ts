import type { components } from '../types/api'
import type { Campaign, CampaignRequestDto, CampaignStatus, CampaignType, Platform } from '../types'
import { CAMPAIGN_STATUS_CONFIG } from '../types'
import { type CampaignFilters, emptyFilters } from '../components/ui/campaign/filters/CampaignFilterTypes'
import { fetchWithAuth, getCurrentUser } from './client'
import { rupeesToPaise } from '../utils/currency'
import { yyyymmddToIso } from '../utils/time'

const API_BASE = '/api/v1'

export interface CampaignStepDto {
  type: string
  label: string
  stepOrder: number
}

let stepConfigCache: Promise<CampaignStepDto[]> | null = null

/** GET /campaigns/step-config — the flat set of screenshot steps selectable when configuring a campaign. */
export function fetchStepConfig(): Promise<CampaignStepDto[]> {
  if (!stepConfigCache) {
    stepConfigCache = fetchWithAuth(`${API_BASE}/campaigns/step-config`)
      .then(r => r.json() as Promise<CampaignStepDto[]>)
  }
  return stepConfigCache
}

/** GET /campaigns/{id}/step-config — the resolved, ordered claim steps for one campaign. */
export function fetchCampaignStepConfig(campaignId: string): Promise<CampaignStepDto[]> {
  return fetchWithAuth(`${API_BASE}/campaigns/${campaignId}/step-config`)
    .then(r => r.json() as Promise<CampaignStepDto[]>)
}

type BackendRequest = components['schemas']['CampaignRequestDto']
export type CampaignResponseDto = components['schemas']['CampaignResponseDto']
type CampaignSummaryDto = components['schemas']['CampaignSummaryResponseDto']

const statusMap: Record<NonNullable<CampaignSummaryDto['status']>, CampaignStatus> = {
  CAMPAIGN_STATUS_DRAFT:     'draft',
  CAMPAIGN_STATUS_ACTIVE:    'active',
  CAMPAIGN_STATUS_ASSIGNED:  'active',
  CAMPAIGN_STATUS_PAUSED:    'paused',
  CAMPAIGN_STATUS_COMPLETED: 'completed',
  CAMPAIGN_STATUS_CLOSED:    'closed',
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
    affiliateLinkAllowed: dto.affiliateLinkAllowed ?? false,
    requiredSteps: dto.requiredSteps as BackendRequest['requiredSteps'],
    ...(dto.commissionToAllPaise ? { commissionToAllPaise: dto.commissionToAllPaise } : {}),
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
        adjustedCampaignPricePaise: dto.campaignPricePaise,
        commissionOfferedPaise: rupeesToPaise(e.commissionOffered),
        slotOffered: e.slotsAvailable,
      })),
    } : {}),
    ...(dto.action ? { action: dto.action as BackendRequest['action'] } : {}),
  }

  const res = await fetchWithAuth(`${API_BASE}/campaigns`, {
    method: 'POST',
    body: JSON.stringify(body),
  })

  return res.json() as Promise<CampaignResponseDto>
}

export interface PagedCampaigns {
  items: Campaign[]
  total: number
  totalPages: number
}

function mapCampaignSummaryDto(dto: CampaignSummaryDto): Campaign {
  return {
    id:                   dto.campaignId ?? '',
    code:                 dto.code ?? '',
    title:                dto.title ?? '',
    status:               statusMap[dto.status ?? 'CAMPAIGN_STATUS_DRAFT'],
    platform:             (dto.platform ?? '') as Platform,
    productBrandName:     dto.productBrandName ?? '',
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
    spent:                0,
    impressions:          0,
    clicks:               0,
    conversions:          0,
    ctr:                  0,
    startDate:            yyyymmddToIso(dto.startDate),
    endDate:              yyyymmddToIso(dto.endDate),
  }
}

export async function searchCampaigns(filters: CampaignFilters, page = 0, size = 10): Promise<PagedCampaigns> {
  const brands = filters.brand
    ? filters.brand.split(',').map(s => s.trim()).filter(Boolean)
    : null
  const platforms = filters.platforms.size > 0 ? [...filters.platforms] : null
  const types = filters.types.size > 0 ? [...filters.types] : null
  const statuses = filters.statuses.size > 0
    ? [...filters.statuses].flatMap(s => CAMPAIGN_STATUS_CONFIG[s].backendStatuses)
    : null
  const body = {
    brands,
    platforms,
    types,
    statuses,
    fromDate: filters.startDate ? isoToYYYYMMDD(filters.startDate) : null,
    toDate: filters.endDate ? isoToYYYYMMDD(filters.endDate) : null,
  }
  const res = await fetchWithAuth(`${API_BASE}/campaigns/search?page=${page}&size=${size}`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  const data = await res.json() as { items?: CampaignSummaryDto[]; total?: number; totalPages?: number }
  return {
    items:      (data.items ?? []).map(mapCampaignSummaryDto),
    total:      data.total ?? 0,
    totalPages: data.totalPages ?? 0,
  }
}

export interface CampaignNameOption {
  id: string
  title: string
  code: string
}

/** GET /campaigns/names — lightweight id+title+code list of campaigns owned by the requester, for typeahead pickers. */
export async function fetchCampaignNames(): Promise<CampaignNameOption[]> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/names`)
  const data = await res.json() as { id?: string; title?: string; code?: string }[]
  return data.map(d => ({ id: d.id ?? '', title: d.title ?? '', code: d.code ?? '' }))
}

/** GET /campaigns/brands — distinct brand names across campaigns owned by the requester. */
export async function fetchBrandNames(): Promise<string[]> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/brands`)
  return (await res.json()) as string[]
}

/** POST /campaigns/shared — lightweight id+title+code list of campaigns shared with the current brand, for typeahead pickers. */
export async function fetchSharedCampaignNames(): Promise<CampaignNameOption[]> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/shared?page=0&size=1000`, {
    method: 'POST',
    body: JSON.stringify({}),
  })
  const data = await res.json() as { items?: CampaignSummaryDto[] }
  return (data.items ?? []).map(d => ({ id: d.campaignId ?? '', title: d.title ?? '', code: d.code ?? '' }))
}

/** POST /campaigns/shared — campaigns shared with the current brand, paginated and filterable. */
export async function fetchSharedCampaigns(
  filters: CampaignFilters = emptyFilters(),
  page = 0,
  size = 10,
): Promise<PagedCampaigns> {
  const brands = filters.brand
    ? filters.brand.split(',').map(s => s.trim()).filter(Boolean)
    : null
  const platforms = filters.platforms.size > 0 ? [...filters.platforms] : null
  const types = filters.types.size > 0 ? [...filters.types] : null
  const statuses = filters.statuses.size > 0
    ? [...filters.statuses].flatMap(s => CAMPAIGN_STATUS_CONFIG[s].backendStatuses)
    : null
  const body = {
    brands,
    platforms,
    types,
    statuses,
    fromDate: filters.startDate ? isoToYYYYMMDD(filters.startDate) : null,
    toDate: filters.endDate ? isoToYYYYMMDD(filters.endDate) : null,
  }
  const res = await fetchWithAuth(`${API_BASE}/campaigns/shared?page=${page}&size=${size}`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  const data = await res.json() as { items?: CampaignSummaryDto[]; total?: number; totalPages?: number }
  return {
    items:      (data.items ?? []).map(mapCampaignSummaryDto),
    total:      data.total ?? 0,
    totalPages: data.totalPages ?? 0,
  }
}

type ShareCampaignRequestDto = components['schemas']['ShareCampaignRequestDto']

/** POST /campaigns/{id}/share — shares a campaign with a connected brand. Cannot be undone. */
export async function shareCampaignWithBrand(campaignId: string, toUserId: string): Promise<void> {
  const body: ShareCampaignRequestDto = { toUserId }
  await fetchWithAuth(`${API_BASE}/campaigns/${campaignId}/share`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function fetchCampaignById(id: string): Promise<CampaignResponseDto> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/${id}`)
  return res.json() as Promise<CampaignResponseDto>
}

export interface CampaignBriefDto {
  id: string
  title: string
  productBrandName: string | null
  platform: string | null
}

export async function fetchCampaignsByIds(ids: string[]): Promise<CampaignBriefDto[]> {
  if (ids.length === 0) return []
  const res = await fetchWithAuth(`${API_BASE}/campaigns/batch`, {
    method: 'POST',
    body: JSON.stringify({ ids }),
  })
  return res.json() as Promise<CampaignBriefDto[]>
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
    affiliateLinkAllowed: dto.affiliateLinkAllowed ?? false,
    requiredSteps: dto.requiredSteps as BackendRequest['requiredSteps'],
    ...(dto.commissionToAllPaise ? { commissionToAllPaise: dto.commissionToAllPaise } : {}),
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
        adjustedCampaignPricePaise: dto.campaignPricePaise,
        commissionOfferedPaise: rupeesToPaise(e.commissionOffered),
        slotOffered: e.slotsAvailable,
      })),
    } : {}),
    ...(dto.action ? { action: dto.action as BackendRequest['action'] } : {}),
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

export async function pauseCampaign(campaignId: string): Promise<CampaignResponseDto> {
  const res = await fetchWithAuth(
    `${API_BASE}/campaigns/${campaignId}/action/CAMPAIGN_ACTION_PAUSE`,
    { method: 'POST' },
  )
  return res.json() as Promise<CampaignResponseDto>
}

export async function resumeCampaign(campaignId: string): Promise<CampaignResponseDto> {
  const res = await fetchWithAuth(
    `${API_BASE}/campaigns/${campaignId}/action/CAMPAIGN_ACTION_RESUME`,
    { method: 'POST' },
  )
  return res.json() as Promise<CampaignResponseDto>
}

export async function closeCampaign(campaignId: string): Promise<CampaignResponseDto> {
  const res = await fetchWithAuth(
    `${API_BASE}/campaigns/${campaignId}/action/CAMPAIGN_ACTION_CLOSE`,
    { method: 'POST' },
  )
  return res.json() as Promise<CampaignResponseDto>
}

export async function deleteCampaign(campaignId: string): Promise<void> {
  await fetchWithAuth(`${API_BASE}/campaigns/${campaignId}`, { method: 'DELETE' })
}

export interface AssignableCampaign {
  campaignId: string
  campaignTitle: string
  code: string
  platform: string
  campaignType: string | null
  productBrandName: string
  productImageUrl: string
  startDate: string
  endDate: string
  campaignPricePaise: number
  slotId: string
  slotsAvailable: number
  totalSlots: number
}

export async function fetchAssignableCampaigns(assigneeId: string): Promise<AssignableCampaign[]> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/assignable?assigneeId=${assigneeId}`)
  const data = await res.json() as components['schemas']['AssignableCampaignResponseDto'][]
  return data.map(d => ({
    campaignId: d.campaignId ?? '',
    campaignTitle: d.campaignTitle ?? '',
    code: d.code ?? '',
    platform: d.platform ?? '',
    campaignType: d.campaignType ?? null,
    productBrandName: d.productBrandName ?? '',
    productImageUrl: d.productImageUrl ?? '',
    startDate: yyyymmddToIso(d.startDate),
    endDate: yyyymmddToIso(d.endDate),
    campaignPricePaise: d.campaignPricePaise ?? 0,
    slotId: d.slotId ?? '',
    slotsAvailable: d.slotsAvailable ?? 0,
    totalSlots: d.totalSlots ?? 0,
  }))
}

export interface ShareableCampaign {
  campaignId: string
  campaignTitle: string
  code: string
  platform: string
  campaignType: string | null
  productBrandName: string
  productImageUrl: string
  startDate: string
  endDate: string
  campaignPricePaise: number
  slotsAvailable: number
  totalSlots: number
}

/** GET /campaigns/shareable — the requester's own campaigns eligible to be shared with a brand. */
export async function fetchShareableCampaigns(): Promise<ShareableCampaign[]> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/shareable`)
  const data = await res.json() as components['schemas']['ShareableCampaignResponseDto'][]
  return data.map(d => ({
    campaignId: d.campaignId ?? '',
    campaignTitle: d.campaignTitle ?? '',
    code: d.code ?? '',
    platform: d.platform ?? '',
    campaignType: d.campaignType ?? null,
    productBrandName: d.productBrandName ?? '',
    productImageUrl: d.productImageUrl ?? '',
    startDate: yyyymmddToIso(d.startDate),
    endDate: yyyymmddToIso(d.endDate),
    campaignPricePaise: d.campaignPricePaise ?? 0,
    slotsAvailable: d.slotsAvailable ?? 0,
    totalSlots: d.totalSlots ?? 0,
  }))
}

export interface SharedByMeCampaign {
  campaignName: string
  campaignCode: string
  sharedWithUserId: string
  sharedWithUserName: string
  sharedWithUserCode: string
  sharedAt: string
}

export interface PagedSharedByMeCampaigns {
  items: SharedByMeCampaign[]
  total: number
  totalPages: number
}

/** GET /campaigns/shared-by-me — campaigns the requester (agency) has already shared, with whom and when. */
export async function fetchCampaignsSharedByMe(page = 0, size = 20): Promise<PagedSharedByMeCampaigns> {
  const res = await fetchWithAuth(`${API_BASE}/campaigns/shared-by-me?page=${page}&size=${size}`)
  const data = await res.json() as components['schemas']['PagedSharedCampaignViewResponseDto']
  return {
    items: (data.items ?? []).map(d => ({
      campaignName: d.campaignName ?? '',
      campaignCode: d.campaignCode ?? '',
      sharedWithUserId: d.sharedWithUserId ?? '',
      sharedWithUserName: d.sharedWithUserName ?? '',
      sharedWithUserCode: d.sharedWithUserCode ?? '',
      sharedAt: d.sharedAt ?? '',
    })),
    total: data.total ?? 0,
    totalPages: data.totalPages ?? 0,
  }
}
