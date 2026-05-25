import type { Deal, Platform, CampaignType } from '../types/DealTypes'
import type { CampaignResponseDto } from './campaignApi'
import { deals, platforms, dealTypes } from '../data/mockData'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../constants/campaigns'

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

export async function fetchClaimedDeals(): Promise<Deal[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return deals.filter(d => d.status === 'claimed').map(toFullDeal)
}

export interface ExploreDealsPage {
  items: Deal[]
  total: number
  page: number
  totalPages: number
}

export const EXPLORE_PAGE_SIZE = 6

export async function fetchExploreDeals(page: number): Promise<ExploreDealsPage> {
  await new Promise(resolve => setTimeout(resolve, 400))
  const exploreDeals = deals.filter(d => d.status === 'explore').map(toFullDeal)
  const total = exploreDeals.length
  const totalPages = Math.ceil(total / EXPLORE_PAGE_SIZE)
  const start = (page - 1) * EXPLORE_PAGE_SIZE
  const items = exploreDeals.slice(start, start + EXPLORE_PAGE_SIZE)
  return { items, total, page, totalPages }
}

export function campaignToDeal(dto: CampaignResponseDto): Deal {
  const platform = (dto.platform ?? 'PLATFORM_AMAZON') as Platform
  const dealType = (dto.campaignType ?? 'CAMPAIGN_TYPE_ORDER') as CampaignType
  return {
    id: dto.id ?? '',
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
