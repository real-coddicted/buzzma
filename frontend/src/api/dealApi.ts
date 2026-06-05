import type { components } from '../types/api'
import type { Deal, Platform, CampaignType } from '../types/DealTypes'
import type { CampaignResponseDto } from './campaignApi'
import { deals, platforms, dealTypes } from '../data/mockData'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../constants/campaigns'
import { fetchWithAuth } from './client'

const API_BASE = '/api/v1'

type DealResponseDto = components['schemas']['DealResponseDto']
type PagedDealsResponseDto = components['schemas']['PagedDealsResponseDto']
type ClaimResponseDto = components['schemas']['ClaimResponseDto']

function toFullDeal(deal: typeof deals[number]): Deal {
  return {
    ...deal,
    platformLabel: platforms.find(p => p.value === deal.platform)?.label ?? deal.platform,
    dealTypeLabel: dealTypes.find(t => t.value === deal.dealType)?.label ?? deal.dealType,
  }
}

export async function fetchDeals(): Promise<Deal[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return deals.map(toFullDeal)
}

export function claimResponseToDeal(dto: ClaimResponseDto): Deal {
  const d = dto.deal ?? {}
  const platform = (d.platform ?? '') as Platform
  const dealType = (d.dealType ?? '') as CampaignType
  return {
    id: d.id ?? '',
    claimId: dto.id ?? '',
    campaignId: d.campaignId ?? '',
    productName: d.productName ?? '',
    productImageUrl: d.productImageUrl ?? '',
    productUrl: d.productUrl ?? '',
    platform,
    platformLabel: PLATFORM_LABELS[platform] ?? platform,
    dealType,
    dealTypeLabel: CAMPAIGN_TYPE_LABELS[dealType] ?? dealType,
    originalPricePaise: d.originalPricePaise ?? 0,
    offeredPricePaise: d.offeredPricePaise ?? 0,
    sellerName: d.sellerName,
    termsAndConditions: d.termsAndConditions,
    status: 'claimed',
    currentStep: dto.currentStep ?? 0,
  }
}

export interface ExploreDealsPage {
  items: Deal[]
  total: number
  page: number
  totalPages: number
}

export const EXPLORE_PAGE_SIZE = 6

function dealResponseToDeal(dto: DealResponseDto): Deal {
  const platform = dto.platform as Platform
  const dealType = dto.dealType as CampaignType
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
    sellerName: dto.sellerName,
    termsAndConditions: dto.termsAndConditions,
    status: 'explore',
  }
}

export async function fetchExploreDeals(page: number): Promise<ExploreDealsPage> {
  // Backend pagination is zero-based; the UI uses 1-based page numbers.
  const params = new URLSearchParams({
    page: String(page - 1),
    size: String(EXPLORE_PAGE_SIZE),
  })
  const res = await fetchWithAuth(`${API_BASE}/deals/unclaimed?${params.toString()}`)
  const data = (await res.json()) as PagedDealsResponseDto
  const items = (data.items ?? []).map(dealResponseToDeal)
  const total = data.total ?? items.length
  const totalPages = data.totalPages ?? Math.max(1, Math.ceil(total / EXPLORE_PAGE_SIZE))
  return { items, total, page: (data.page ?? page - 1) + 1, totalPages }
}

export function campaignToDeal(dto: CampaignResponseDto): Deal {
  const platform = dto.platform as Platform
  const dealType = dto.campaignType as CampaignType
  return {
    id: dto.id ?? '',
    campaignId: dto.id ?? '',
    productName: dto.productName ?? '',
    productImageUrl: dto.productImageUrl ?? '',
    productUrl: dto.productLink ?? '',
    platform,
    platformLabel: PLATFORM_LABELS[platform] ?? platform,
    dealType,
    dealTypeLabel: CAMPAIGN_TYPE_LABELS[dealType] ?? dealType,
    originalPricePaise: dto.productPricePaise ?? 0,
    offeredPricePaise: dto.campaignPricePaise ?? 0,
    sellerName: dto.sellerName,
    termsAndConditions: dto.termsAndConditions,
    status: 'claimed',
  }
}
