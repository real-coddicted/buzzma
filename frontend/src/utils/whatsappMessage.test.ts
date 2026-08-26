import { describe, it, expect, afterEach, vi } from 'vitest'
import { buildWhatsAppMessage, buildDealShareUrl } from './whatsappMessage'
import type { Deal } from '../types/DealTypes'

const baseDeal: Deal = {
  id: 'd1',
  code: 'X3NK-BHP2',
  campaignId: 'c1',
  productName: 'boAt Rockerz 450 Wireless Headphones',
  productImageUrl: '',
  productUrl: 'https://www.amazon.in/dp/B08CF1XZY6',
  platform: 'PLATFORM_AMAZON',
  platformLabel: 'Amazon',
  dealType: 'CAMPAIGN_TYPE_DISCOUNT',
  dealTypeLabel: 'Discount Deal',
  originalPricePaise: 199900,
  offeredPricePaise: 49900,
  status: 'explore',
}

describe('buildWhatsAppMessage', () => {
  it('includes the core deal fields', () => {
    const message = buildWhatsAppMessage(baseDeal, 'Priya Sharma')
    expect(message).toContain('Product: boAt Rockerz 450 Wireless Headphones')
    expect(message).toContain('Deal Type: Discount Deal')
    expect(message).toContain('Deal Code: X3NK-BHP2')
    expect(message).toContain('Platform: Amazon')
    expect(message).toContain('https://buzzmah.com/deals?view=detail&id=d1')
    expect(message).not.toContain('https://www.amazon.in/dp/B08CF1XZY6')
    expect(message).toContain('Mediator Name: Priya Sharma')
    expect(message).toContain('Original Price: ₹1,999.00/-')
    expect(message).toContain('Offer Price: ₹499.00/-')
  })

  it('omits the Note section when there are no terms', () => {
    const message = buildWhatsAppMessage(baseDeal, 'Priya Sharma')
    expect(message).not.toContain('📌 Note:')
  })

  it('renders each terms line as a bullet under Note', () => {
    const message = buildWhatsAppMessage(
      {
        ...baseDeal,
        termsAndConditions:
          'If filling refund form after 40 days, no refund amount will be issued.\nFor Amazon, in case of Delete/Error: 50% refund will be paid with unverified review.',
      },
      'Priya Sharma',
    )
    expect(message).toContain('📌 Note:')
    expect(message).toContain('If filling refund form after 40 days, no refund amount will be issued.')
    expect(message).toContain('For Amazon, in case of Delete/Error: 50% refund will be paid with unverified review.')
  })
})

describe('buildDealShareUrl', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('falls back to the default buzzmah.com domain when no base URL is configured, URL-encoding the id', () => {
    expect(buildDealShareUrl('d1')).toBe('https://buzzmah.com/deals?view=detail&id=d1')
    expect(buildDealShareUrl('a/b c')).toBe('https://buzzmah.com/deals?view=detail&id=a%2Fb%20c')
  })

  it('prefers window.__APP_BASE_URL__ (injected by the Cloudflare Worker) when set', () => {
    vi.stubGlobal('window', { __APP_BASE_URL__: 'https://test.buzzmah.com' })
    expect(buildDealShareUrl('d1')).toBe('https://test.buzzmah.com/deals?view=detail&id=d1')
  })
})
