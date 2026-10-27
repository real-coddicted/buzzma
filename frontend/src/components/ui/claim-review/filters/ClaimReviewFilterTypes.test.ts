import { describe, it, expect } from 'vitest'
import { emptyFilters, countActiveFilters, matchesFilters } from './ClaimReviewFilterTypes'
import type { ClaimReviewItem } from '../../../../types'

function makeRow(overrides: Partial<ClaimReviewItem> = {}): ClaimReviewItem {
  return {
    id: '1',
    claimCode: 'CLAM-X3NK',
    campaignId: 'camp-1',
    campaignName: 'Summer Sale',
    orderId: 'ORD-1',
    orderDate: '2026-01-01',
    mediatorId: 'med-1',
    mediatorName: 'Alice',
    buyerName: 'Bob',
    claimStatus: 'ORDERED',
    approvalMethod: 'manual',
    mediatorVerified: false,
    brandVerified: false,
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
      claimStatuses: new Set<ClaimReviewItem['claimStatus']>([]),
      mediatorIds: new Set(['med-1']),
      claimCode: '',
    }
    expect(countActiveFilters(f)).toBe(5)
  })

  it('counts a non-blank claim code as one active filter', () => {
    const f = { ...emptyFilters(), claimCode: 'CLAM-X3NK' }
    expect(countActiveFilters(f)).toBe(1)
  })

  it('does not count a blank or whitespace-only claim code', () => {
    const f = { ...emptyFilters(), claimCode: '   ' }
    expect(countActiveFilters(f)).toBe(0)
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

  it('filters by claim status', () => {
    const f = { ...emptyFilters(), claimStatuses: new Set<ClaimReviewItem['claimStatus']>(['APPROVED']) }
    expect(matchesFilters(makeRow(), f)).toBe(false)
    expect(matchesFilters(makeRow({ claimStatus: 'APPROVED' }), f)).toBe(true)
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

  it('filters by claim code, exact match case-insensitive', () => {
    const f = { ...emptyFilters(), claimCode: 'clam-x3nk' }
    expect(matchesFilters(makeRow({ claimCode: 'CLAM-X3NK' }), f)).toBe(true)
    expect(matchesFilters(makeRow({ claimCode: 'CLAM-OTHER' }), f)).toBe(false)
  })

  it('does not match claim code by substring', () => {
    const f = { ...emptyFilters(), claimCode: 'X3NK' }
    expect(matchesFilters(makeRow({ claimCode: 'CLAM-X3NK' }), f)).toBe(false)
  })
})
