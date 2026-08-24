import { describe, it, expect } from 'vitest'
import { isDealSoldOut } from './DealTypes'
import type { Deal } from './DealTypes'

function makeDeal(overrides: Partial<Deal> = {}): Deal {
  return {
    id: 'deal-1',
    campaignId: 'camp-1',
    productName: 'Test Product',
    productImageUrl: 'https://example.com/img.png',
    productUrl: 'https://example.com/product',
    platform: 'PLATFORM_AMAZON',
    platformLabel: 'Amazon',
    dealType: 'CAMPAIGN_TYPE_REVIEW',
    dealTypeLabel: 'Review',
    originalPricePaise: 10000,
    offeredPricePaise: 8000,
    status: 'explore',
    ...overrides,
  }
}

describe('isDealSoldOut', () => {
  it('is true when slotsAvailable is 0', () => {
    expect(isDealSoldOut(makeDeal({ slotsAvailable: 0 }))).toBe(true)
  })

  it('is false when slotsAvailable is positive', () => {
    expect(isDealSoldOut(makeDeal({ slotsAvailable: 5 }))).toBe(false)
  })

  it('is false when slotsAvailable is absent', () => {
    expect(isDealSoldOut(makeDeal({ slotsAvailable: undefined }))).toBe(false)
  })
})
