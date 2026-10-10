import { describe, it, expect } from 'vitest'
import { PROMOTION_CATEGORY_PLATFORMS, PROMOTION_CATEGORY_CAMPAIGN_TYPES, PROMOTION_CATEGORY_STEPS } from './campaigns'

describe('Quick Commerce promotion category constants', () => {
  it('allows only BigBasket, Blinkit and Zepto as platforms', () => {
    expect(PROMOTION_CATEGORY_PLATFORMS.QUICK_COMMERCE).toEqual([
      'PLATFORM_BIGBASKET',
      'PLATFORM_BLINKIT',
      'PLATFORM_ZEPTO',
    ])
  })

  it('does not allow the exchange campaign type', () => {
    expect(PROMOTION_CATEGORY_CAMPAIGN_TYPES.QUICK_COMMERCE).not.toContain('CAMPAIGN_TYPE_EXCHANGE')
  })

  it('does not allow seller feedback or return window steps', () => {
    expect(PROMOTION_CATEGORY_STEPS.QUICK_COMMERCE).not.toContain('SELLER_FEEDBACK')
    expect(PROMOTION_CATEGORY_STEPS.QUICK_COMMERCE).not.toContain('RETURN_WINDOW')
    expect(PROMOTION_CATEGORY_STEPS.QUICK_COMMERCE).toEqual(['ORDER', 'DELIVERY', 'RATING', 'REVIEW'])
  })
})
