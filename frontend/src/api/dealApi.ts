import type { Deal } from '../types/DealTypes'
import { deals, platforms, dealTypes } from '../data/mockData'

export async function fetchDeals(): Promise<Deal[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return deals.map(deal => ({
    ...deal,
    platformLabel: platforms.find(p => p.value === deal.platform)?.label ?? deal.platform,
    dealTypeLabel: dealTypes.find(t => t.value === deal.dealType)?.label ?? deal.dealType,
  }))
}
