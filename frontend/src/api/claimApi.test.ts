import { describe, it, expect } from 'vitest'
import { mapClaim, mapPagedClaimsResponse, mergeReviewedClaim } from './claimApi'
import type { components } from '../types/api'
import type { ClaimReviewItem } from '../types/ClaimReviewTypes'

type PagedClaimsResponseDto = components['schemas']['PagedClaimsResponseDto']
type ClaimResponseDto = components['schemas']['ClaimResponseDto']

function makeClaimDto(overrides: Partial<ClaimResponseDto> = {}): ClaimResponseDto {
  return {
    id: 'claim-1',
    code: 'CLM1-A2B3',
    status: 'ORDERED',
    ...overrides,
  }
}

function makeClaimReviewItem(overrides: Partial<ClaimReviewItem> = {}): ClaimReviewItem {
  return {
    id: 'claim-1',
    campaignId: 'campaign-1',
    campaignName: 'Campaign One',
    orderId: 'order-1',
    orderDate: '2026-01-01',
    mediatorId: 'mediator-1',
    mediatorName: 'Mediator One',
    buyerName: 'Buyer One',
    claimStatus: 'ORDERED',
    approvalMethod: 'manual',
    mediatorVerified: false,
    brandVerified: false,
    matchPct: 80,
    platform: 'PLATFORM_AMAZON',
    brandName: 'Brand One',
    ...overrides,
  }
}

describe('mapPagedClaimsResponse', () => {
  it('converts the backend 0-based page to a 1-based page', () => {
    const dto: PagedClaimsResponseDto = { items: [], total: 0, page: 0, totalPages: 1 }
    expect(mapPagedClaimsResponse(dto).page).toBe(1)
  })

  it('carries items and total through unchanged', () => {
    const claim = makeClaimDto()
    const dto: PagedClaimsResponseDto = { items: [claim], total: 1, page: 0, totalPages: 1 }
    const result = mapPagedClaimsResponse(dto)
    expect(result.items).toEqual([claim])
    expect(result.total).toBe(1)
  })

  it('falls back to empty items and zero total when absent', () => {
    const result = mapPagedClaimsResponse({})
    expect(result.items).toEqual([])
    expect(result.total).toBe(0)
    expect(result.page).toBe(1)
    expect(result.totalPages).toBe(1)
  })
})

describe('mapClaim', () => {
  it('carries exchangeProduct through when present', () => {
    const result = mapClaim(makeClaimDto({ exchangeProduct: 'Wireless Earbuds' }))
    expect(result.exchangeProduct).toBe('Wireless Earbuds')
  })

  it('leaves exchangeProduct undefined when absent', () => {
    const result = mapClaim(makeClaimDto())
    expect(result.exchangeProduct).toBeUndefined()
  })
})

describe('mergeReviewedClaim', () => {
  it('preserves brandName, buyerName, campaignName, and mediatorName from current when updated has them blank', () => {
    const current = makeClaimReviewItem()
    const updated = makeClaimReviewItem({
      brandName: '',
      buyerName: '',
      campaignName: '',
      mediatorName: '',
      claimStatus: 'APPROVED',
      matchPct: 95,
    })
    const result = mergeReviewedClaim(current, updated)
    expect(result.brandName).toBe(current.brandName)
    expect(result.buyerName).toBe(current.buyerName)
    expect(result.campaignName).toBe(current.campaignName)
    expect(result.mediatorName).toBe(current.mediatorName)
  })

  it('takes other fields from updated', () => {
    const current = makeClaimReviewItem()
    const updated = makeClaimReviewItem({ claimStatus: 'APPROVED', matchPct: 95 })
    const result = mergeReviewedClaim(current, updated)
    expect(result.claimStatus).toBe('APPROVED')
    expect(result.matchPct).toBe(95)
  })
})
