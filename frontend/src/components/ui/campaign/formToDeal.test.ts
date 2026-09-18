import { describe, it, expect } from 'vitest'
import { EMPTY_FORM, type CampaignForm, type ExchangeProductRow } from './campaignFormConstants'
import { campaignFormToDeal } from './formToDeal'

function baseForm(overrides: Partial<CampaignForm> = {}): CampaignForm {
  return {
    ...EMPTY_FORM,
    title: 'Wireless Mouse Deal',
    platform: 'PLATFORM_AMAZON',
    productName: 'Wireless Mouse',
    productImageUrl: 'https://example.com/mouse.jpg',
    productUrl: 'https://example.com/mouse',
    sellerName: 'Acme Seller',
    originalPriceRupees: '1000',
    campaignPriceRupees: '800',
    campaignType: 'CAMPAIGN_TYPE_EXCHANGE',
    startDate: '2026-01-01',
    endDate: '2026-02-01',
    totalSlots: '10',
    requiredSteps: ['ORDER', 'REVIEW'],
    ...overrides,
  }
}

function exchangeRow(overrides: Partial<ExchangeProductRow> = {}): ExchangeProductRow {
  return { productName: 'Exchange Item', productImageUrl: 'https://example.com/exchange.jpg', selected: true, prefilled: false, ...overrides }
}

describe('campaignFormToDeal', () => {
  it('maps a fully filled form to a Deal with correct paise conversion, labels and image list', () => {
    const deal = campaignFormToDeal(baseForm({ exchangeProducts: [exchangeRow()] }))

    expect(deal.originalPricePaise).toBe(100000)
    expect(deal.offeredPricePaise).toBe(80000)
    expect(deal.platform).toBe('PLATFORM_AMAZON')
    expect(deal.platformLabel).not.toBe('')
    expect(deal.dealTypeLabel).not.toBe('')
    expect(deal.productImages).toEqual(['https://example.com/mouse.jpg', 'https://example.com/exchange.jpg'])
    expect(deal.slotsAvailable).toBe(10)
    expect(deal.requiredSteps).toEqual(['ORDER', 'REVIEW'])
    expect(deal.status).toBe('explore')
  })

  it('falls back to 0 paise and undefined slots for empty/invalid numeric fields, without NaN leaking through', () => {
    const deal = campaignFormToDeal(baseForm({
      originalPriceRupees: '',
      campaignPriceRupees: 'not-a-number',
      totalSlots: '',
    }))

    expect(deal.originalPricePaise).toBe(0)
    expect(deal.offeredPricePaise).toBe(0)
    expect(deal.slotsAvailable).toBeUndefined()
    expect(Number.isNaN(deal.originalPricePaise)).toBe(false)
    expect(Number.isNaN(deal.offeredPricePaise)).toBe(false)
  })

  it('only includes selected exchange products in the image list', () => {
    const deal = campaignFormToDeal(baseForm({
      exchangeProducts: [
        exchangeRow({ productImageUrl: 'https://example.com/selected.jpg', selected: true }),
        exchangeRow({ productImageUrl: 'https://example.com/unselected.jpg', selected: false }),
      ],
    }))

    expect(deal.productImages).toEqual(['https://example.com/mouse.jpg', 'https://example.com/selected.jpg'])
  })
})
