import type { Deal } from '../types/DealTypes'
import { deals } from '../data/mockData'

export async function fetchDeals(): Promise<Deal[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return deals
}
