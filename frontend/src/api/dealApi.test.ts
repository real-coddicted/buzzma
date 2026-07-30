import { describe, it, expect } from 'vitest'
import { dealResponseToDeal, claimResponseToDeal } from './dealApi'
import type { components } from '../types/api'

type DealResponseDto = components['schemas']['DealResponseDto']
type ClaimResponseDto = components['schemas']['ClaimResponseDto']

function makeDto(overrides: Partial<DealResponseDto> = {}): DealResponseDto {
  return {
    id: 'deal-1',
    code: 'DEAL123',
    campaignId: 'camp-1',
    productName: 'Test Product',
    productImageUrl: 'https://example.com/img.png',
    productUrl: 'https://example.com/product',
    platform: 'PLATFORM_AMAZON',
    dealType: 'CAMPAIGN_TYPE_REVIEW',
    originalPricePaise: 10000,
    offeredPricePaise: 8000,
    slotsAvailable: 5,
    ...overrides,
  }
}

function makeClaimDto(overrides: Partial<ClaimResponseDto> = {}): ClaimResponseDto {
  return {
    id: 'claim-1',
    code: 'CLM1-A2B3',
    deal: makeDto(),
    status: 'ORDERED',
    ...overrides,
  }
}

describe('dealResponseToDeal', () => {
  it('maps ownerName to mediatorName', () => {
    const deal = dealResponseToDeal(makeDto({ ownerName: 'Alice Mediator' }))
    expect(deal.mediatorName).toBe('Alice Mediator')
  })

  it('leaves mediatorName undefined when ownerName is absent', () => {
    const deal = dealResponseToDeal(makeDto())
    expect(deal.mediatorName).toBeUndefined()
  })

  it('carries the deal code through unchanged', () => {
    const deal = dealResponseToDeal(makeDto({ code: 'ABC999' }))
    expect(deal.code).toBe('ABC999')
  })
})

describe('claimResponseToDeal', () => {
  it('maps the claim code to claimCode', () => {
    const deal = claimResponseToDeal(makeClaimDto({ code: 'CLM1-A2B3' }))
    expect(deal.claimCode).toBe('CLM1-A2B3')
  })

  it('keeps the deal code separate from the claim code', () => {
    const deal = claimResponseToDeal(makeClaimDto({ code: 'CLM1-A2B3', deal: makeDto({ code: 'DEAL123' }) }))
    expect(deal.code).toBe('DEAL123')
    expect(deal.claimCode).toBe('CLM1-A2B3')
  })
})