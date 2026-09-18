import { describe, it, expect } from 'vitest'
import {
  EMPTY_FORM,
  validateCampaignForm,
  mergeExchangeProductRows,
  isValidImageUrl,
  type CampaignForm,
  type ExchangeProductRow,
} from './campaignFormConstants'

function exchangeForm(exchangeProducts: ExchangeProductRow[]): CampaignForm {
  return {
    ...EMPTY_FORM,
    title: 'Exchange campaign',
    platform: 'PLATFORM_AMAZON',
    productBrandName: 'Acme',
    productName: 'Acme Cup',
    productImageUrl: 'https://example.com/cup.jpg',
    productUrl: 'https://example.com/cup',
    startDate: '2999-01-01',
    endDate: '2999-02-01',
    originalPriceRupees: '100',
    campaignPriceRupees: '80',
    campaignType: 'CAMPAIGN_TYPE_EXCHANGE',
    exchangeProducts,
  }
}

describe('isValidImageUrl', () => {
  it('allows an empty value', () => {
    expect(isValidImageUrl('')).toBe(true)
    expect(isValidImageUrl('   ')).toBe(true)
  })

  it('accepts http(s) URLs', () => {
    expect(isValidImageUrl('https://cdn.example.com/a.png')).toBe(true)
    expect(isValidImageUrl('http://example.com/a.png')).toBe(true)
  })

  it('rejects non-http schemes and unparseable strings', () => {
    expect(isValidImageUrl('ftp://example.com/a.png')).toBe(false)
    expect(isValidImageUrl('not a url')).toBe(false)
  })
})

function slotsForm(totalSlots: string): CampaignForm {
  return {
    ...EMPTY_FORM,
    title: 'Slots campaign',
    platform: 'PLATFORM_AMAZON',
    productBrandName: 'Acme',
    productName: 'Acme Cup',
    productImageUrl: 'https://example.com/cup.jpg',
    productUrl: 'https://example.com/cup',
    startDate: '2999-01-01',
    endDate: '2999-02-01',
    originalPriceRupees: '100',
    campaignPriceRupees: '80',
    campaignType: 'CAMPAIGN_TYPE_ORDER',
    totalSlots,
  }
}

describe('validateCampaignForm — total slots', () => {
  it('flags a blank total slots as required', () => {
    expect(validateCampaignForm(slotsForm('')).totalSlots).toBe('Required')
  })

  it('rejects zero, negative or non-numeric total slots', () => {
    expect(validateCampaignForm(slotsForm('0')).totalSlots).toBe('Must be a positive integer')
    expect(validateCampaignForm(slotsForm('-5')).totalSlots).toBe('Must be a positive integer')
    expect(validateCampaignForm(slotsForm('abc')).totalSlots).toBe('Must be a positive integer')
  })

  it('accepts a positive integer total slots', () => {
    expect(validateCampaignForm(slotsForm('100')).totalSlots).toBeUndefined()
  })
})

describe('validateCampaignForm — exchange products', () => {
  it('errors when the exchange type has no selected, named row', () => {
    const e = validateCampaignForm(exchangeForm([
      { productName: 'Widget', productImageUrl: '', selected: false, prefilled: true },
    ]))
    expect(e.exchangeProducts).toBeDefined()
  })

  it('errors when a selected row has a malformed image URL', () => {
    const e = validateCampaignForm(exchangeForm([
      { productName: 'Widget', productImageUrl: 'javascript:alert(1)', selected: true, prefilled: true },
    ]))
    expect(e.exchangeProducts).toBeDefined()
  })

  it('passes with one selected, named row and a blank URL', () => {
    const e = validateCampaignForm(exchangeForm([
      { productName: 'Widget', productImageUrl: '', selected: true, prefilled: true },
    ]))
    expect(e.exchangeProducts).toBeUndefined()
  })

  it('ignores lingering rows for a non-exchange campaign type', () => {
    const form = { ...exchangeForm([
      { productName: 'Widget', productImageUrl: '', selected: false, prefilled: true },
    ]), campaignType: 'CAMPAIGN_TYPE_ORDER' as const }
    const e = validateCampaignForm(form)
    expect(e.exchangeProducts).toBeUndefined()
  })
})

describe('mergeExchangeProductRows', () => {
  it('turns master entries into locked, unchecked rows', () => {
    const rows = mergeExchangeProductRows([{ name: 'Widget' }, { name: 'Gadget' }], [])
    expect(rows).toEqual([
      { productName: 'Widget', productImageUrl: '', selected: false, prefilled: true },
      { productName: 'Gadget', productImageUrl: '', selected: false, prefilled: true },
    ])
  })

  it('reuses a saved row that matches by name ignoring case, checking it', () => {
    const rows = mergeExchangeProductRows(
      [{ name: 'Widget' }],
      [{ productName: 'widget', productImageUrl: 'https://x/w.png', selected: true, prefilled: true }],
    )
    expect(rows).toEqual([
      { productName: 'Widget', productImageUrl: 'https://x/w.png', selected: true, prefilled: true },
    ])
  })

  it('retains a saved row that is absent from the master list', () => {
    const rows = mergeExchangeProductRows(
      [{ name: 'Widget' }],
      [{ productName: 'Legacy', productImageUrl: '', selected: true, prefilled: true }],
    )
    expect(rows).toEqual([
      { productName: 'Widget', productImageUrl: '', selected: false, prefilled: true },
      { productName: 'Legacy', productImageUrl: '', selected: true, prefilled: true },
    ])
  })
})
