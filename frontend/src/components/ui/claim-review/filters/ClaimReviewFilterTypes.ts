import type { ClaimReviewItem, Platform, ClaimStatus } from '../../../../types'

export interface ClaimReviewFilters {
  campaignIds: Set<string>
  brands: Set<string>
  platforms: Set<Platform>
  claimStatuses: Set<ClaimStatus>
  mediatorIds: Set<string>
  claimCode: string
}

export function emptyFilters(): ClaimReviewFilters {
  return {
    campaignIds: new Set(),
    brands: new Set(),
    platforms: new Set(),
    claimStatuses: new Set(),
    mediatorIds: new Set(),
    claimCode: '',
  }
}

export function countActiveFilters(f: ClaimReviewFilters): number {
  return f.campaignIds.size + f.brands.size + f.platforms.size + f.claimStatuses.size + f.mediatorIds.size
    + (f.claimCode.trim() ? 1 : 0)
}

export function matchesFilters(row: ClaimReviewItem, f: ClaimReviewFilters): boolean {
  const claimCode = f.claimCode.trim()
  return (
    (f.campaignIds.size === 0 || f.campaignIds.has(row.campaignId)) &&
    (f.brands.size === 0 || f.brands.has(row.brandName)) &&
    (f.platforms.size === 0 || f.platforms.has(row.platform)) &&
    (f.claimStatuses.size === 0 || f.claimStatuses.has(row.claimStatus)) &&
    (f.mediatorIds.size === 0 || f.mediatorIds.has(row.mediatorId)) &&
    (!claimCode || (row.claimCode ?? '').toLowerCase() === claimCode.toLowerCase())
  )
}