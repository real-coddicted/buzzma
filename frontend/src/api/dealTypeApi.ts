import type { DealTypeOption } from '../types/DealTypes'
import { dealTypes } from '../data/mockData'

export async function fetchDealTypes(): Promise<DealTypeOption[]> {
  await new Promise(resolve => setTimeout(resolve, 300))
  return dealTypes
}
