import { describe, it, expect } from 'vitest'
import { paiseToRupees, rupeesToPaise, formatRupees } from './currency'

describe('paiseToRupees', () => {
  it('converts zero', () => {
    expect(paiseToRupees(0)).toBe(0)
  })

  it('converts whole rupees', () => {
    expect(paiseToRupees(100)).toBe(1)
    expect(paiseToRupees(10000)).toBe(100)
  })

  it('converts paise-only amounts', () => {
    expect(paiseToRupees(50)).toBe(0.5)
    expect(paiseToRupees(5)).toBe(0.05)
  })

  it('converts mixed rupees and paise', () => {
    expect(paiseToRupees(123)).toBe(1.23)
    expect(paiseToRupees(9999)).toBe(99.99)
  })
})

describe('rupeesToPaise', () => {
  it('converts whole rupees', () => {
    expect(rupeesToPaise(1)).toBe(100)
    expect(rupeesToPaise(100)).toBe(10000)
  })

  it('converts two decimal places', () => {
    expect(rupeesToPaise(1.23)).toBe(123)
    expect(rupeesToPaise(99.99)).toBe(9999)
  })

  it('converts one decimal place', () => {
    expect(rupeesToPaise(1.5)).toBe(150)
    expect(rupeesToPaise(0.5)).toBe(50)
  })

  it('converts zero', () => {
    expect(rupeesToPaise(0)).toBe(0)
  })

  it('handles negative values', () => {
    expect(rupeesToPaise(-1.23)).toBe(-123)
    expect(rupeesToPaise(-0.5)).toBe(-50)
  })

  it('avoids floating-point multiplication errors', () => {
    // 1.23 * 100 in floating-point gives 123.00000000000001
    expect(rupeesToPaise(1.23)).toBe(123)
    expect(rupeesToPaise(10.10)).toBe(1010)
  })

  it('is the inverse of paiseToRupees', () => {
    const cases = [0, 1, 50, 99, 100, 123, 9999, 100000]
    for (const paise of cases) {
      expect(rupeesToPaise(paiseToRupees(paise))).toBe(paise)
    }
  })
})

describe('formatRupees', () => {
  it('formats zero', () => {
    expect(formatRupees(0)).toBe('0.00')
  })

  it('formats whole rupees', () => {
    expect(formatRupees(1)).toBe('1.00')
    expect(formatRupees(100)).toBe('100.00')
  })

  it('formats with two decimal places', () => {
    expect(formatRupees(1.23)).toBe('1.23')
    expect(formatRupees(99.99)).toBe('99.99')
  })

  it('always shows exactly two decimal places', () => {
    expect(formatRupees(2)).toBe('2.00')
    expect(formatRupees(2.1)).toBe('2.10')
  })

  it('uses en-IN grouping for large amounts', () => {
    expect(formatRupees(1000)).toBe('1,000.00')
    expect(formatRupees(100000)).toBe('1,00,000.00')
  })
})
