import { describe, it, expect } from 'vitest'
import { isValidMobile } from './mobileValidation'

describe('isValidMobile', () => {
  it('accepts exactly 10 digits', () => {
    expect(isValidMobile('9876543210')).toBe(true)
  })

  it('rejects fewer than 10 digits', () => {
    expect(isValidMobile('987654321')).toBe(false)
  })

  it('rejects more than 10 digits', () => {
    expect(isValidMobile('98765432101')).toBe(false)
    expect(isValidMobile('987654321012')).toBe(false)
  })

  it('rejects a leading +', () => {
    expect(isValidMobile('+919876543210')).toBe(false)
  })

  it('rejects non-digit characters', () => {
    expect(isValidMobile('98765abcde')).toBe(false)
  })

  it('ignores whitespace when counting digits', () => {
    expect(isValidMobile('98765 43210')).toBe(true)
  })

  it('rejects an empty string', () => {
    expect(isValidMobile('')).toBe(false)
  })
})
