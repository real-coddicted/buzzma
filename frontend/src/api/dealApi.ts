import type { Deal } from '../types/DealTypes'
import { deals, platforms, dealTypes } from '../data/mockData'

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
