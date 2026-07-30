import { describe, it, expect } from 'vitest'
import { dealResponseToDeal } from './dealApi'
import type { components } from '../types/api'

type DealResponseDto = components['schemas']['DealResponseDto']

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