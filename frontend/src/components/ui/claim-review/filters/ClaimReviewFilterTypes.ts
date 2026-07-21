import type { ClaimReviewItem, Platform, ReviewStatus } from '../../../../types'

export interface ClaimReviewFilters {
  campaignIds: Set<string>
  brands: Set<string>
  platforms: Set<Platform>
  reviewStatuses: Set<ReviewStatus>
  mediatorIds: Set<string>
}

export function emptyFilters(): ClaimReviewFilters {
  return {
    campaignIds: new Set(),
    brands: new Set(),
    platforms: new Set(),
    reviewStatuses: new Set(),
    mediatorIds: new Set(),
  }
}

export function countActiveFilters(f: ClaimReviewFilters): number {
  return f.campaignIds.size + f.brands.size + f.platforms.size + f.reviewStatuses.size + f.mediatorIds.size
}

export function matchesFilters(row: ClaimReviewItem, f: ClaimReviewFilters): boolean {
  return (
    (f.campaignIds.size === 0 || f.campaignIds.has(row.campaignId)) &&
    (f.brands.size === 0 || f.brands.has(row.brandName)) &&
    (f.platforms.size === 0 || f.platforms.has(row.platform)) &&
    (f.reviewStatuses.size === 0 || f.reviewStatuses.has(row.reviewStatus)) &&
    (f.mediatorIds.size === 0 || f.mediatorIds.has(row.mediatorId))
  )
}
