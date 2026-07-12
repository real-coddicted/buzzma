import type { CampaignStatus, Platform, CampaignType } from '../../../../types'

export interface CampaignFilters {
  brand: string
  platforms: Set<Platform>
  types: Set<CampaignType>
  statuses: Set<CampaignStatus>
  startDate: string
  endDate: string
}

export function emptyFilters(): CampaignFilters {
  return {
    brand: '',
    platforms: new Set(),
    types: new Set(),
    statuses: new Set(),
    startDate: '',
    endDate: '',
  }
}

export function countActiveFilters(f: CampaignFilters): number {
  return (
    (f.brand ? 1 : 0) +
    f.platforms.size +
    f.types.size +
    f.statuses.size +
    (f.startDate || f.endDate ? 1 : 0)
  )
}
