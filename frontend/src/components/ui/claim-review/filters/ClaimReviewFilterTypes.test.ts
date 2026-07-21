import { describe, it, expect } from 'vitest'
import { emptyFilters, countActiveFilters, matchesFilters } from './ClaimReviewFilterTypes'
import type { ClaimReviewItem } from '../../../../types'

function makeRow(overrides: Partial<ClaimReviewItem> = {}): ClaimReviewItem {
  return {
    id: '1',
    campaignId: 'camp-1',
    campaignName: 'Summer Sale',
    orderId: 'ORD-1',
    orderDate: '2026-01-01',
    mediatorId: 'med-1',
    mediatorName: 'Alice',
    buyerName: 'Bob',
    claimStatus: 'in-progress',
    reviewStatus: 'pending',
    approvalMethod: 'manual',
    mediatorVerified: false,
    matchPct: 90,
    platform: 'PLATFORM_AMAZON',
    brandName: 'Nike',
    ...overrides,
  }
}

describe('emptyFilters', () => {
  it('returns all-empty sets', () => {
    const f = emptyFilters()
    expect(countActiveFilters(f)).toBe(0)
  })
})

describe('countActiveFilters', () => {
  it('sums the size of every filter set', () => {
    const f = {
      campaignIds: new Set(['camp-1']),
      brands: new Set(['Nike', 'Adidas']),
      platforms: new Set<ClaimReviewItem['platform']>(['PLATFORM_AMAZON']),
      reviewStatuses: new Set<ClaimReviewItem['reviewStatus']>([]),
      mediatorIds: new Set(['med-1']),
    }
    expect(countActiveFilters(f)).toBe(5)
  })
})

describe('matchesFilters', () => {
  it('matches everything when no filters are active', () => {
    expect(matchesFilters(makeRow(), emptyFilters())).toBe(true)
  })

  it('filters by campaign', () => {
    const f = { ...emptyFilters(), campaignIds: new Set(['camp-other']) }
    expect(matchesFilters(makeRow(), f)).toBe(false)
    expect(matchesFilters(makeRow({ campaignId: 'camp-other' }), f)).toBe(true)
  })

  it('filters by brand', () => {
    const f = { ...emptyFilters(), brands: new Set(['Adidas']) }
    expect(matchesFilters(makeRow(), f)).toBe(false)
    expect(matchesFilters(makeRow({ brandName: 'Adidas' }), f)).toBe(true)
  })

  it('filters by platform', () => {
    const f = { ...emptyFilters(), platforms: new Set<ClaimReviewItem['platform']>(['PLATFORM_FLIPKART']) }
    expect(matchesFilters(makeRow(), f)).toBe(false)
    expect(matchesFilters(makeRow({ platform: 'PLATFORM_FLIPKART' }), f)).toBe(true)
  })

  it('filters by review status', () => {
    const f = { ...emptyFilters(), reviewStatuses: new Set<ClaimReviewItem['reviewStatus']>(['approved']) }
    expect(matchesFilters(makeRow(), f)).toBe(false)
    expect(matchesFilters(makeRow({ reviewStatus: 'approved' }), f)).toBe(true)
  })

  it('filters by mediator id, not name', () => {
    const f = { ...emptyFilters(), mediatorIds: new Set(['med-1']) }
    expect(matchesFilters(makeRow({ mediatorId: 'med-1', mediatorName: 'Alice' }), f)).toBe(true)
    expect(matchesFilters(makeRow({ mediatorId: 'med-2', mediatorName: 'Alice' }), f)).toBe(false)
  })

  it('requires all active filter dimensions to match', () => {
    const f = { ...emptyFilters(), brands: new Set(['Nike']), platforms: new Set<ClaimReviewItem['platform']>(['PLATFORM_AMAZON']) }
    expect(matchesFilters(makeRow({ brandName: 'Nike', platform: 'PLATFORM_AMAZON' }), f)).toBe(true)
    expect(matchesFilters(makeRow({ brandName: 'Nike', platform: 'PLATFORM_FLIPKART' }), f)).toBe(false)
  })
})
