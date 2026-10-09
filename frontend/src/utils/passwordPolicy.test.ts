import { describe, it, expect } from 'vitest'
import { getPasswordError, PASSWORD_RULES } from './passwordPolicy'

describe('getPasswordError', () => {
  it('accepts a password meeting every rule', () => {
    expect(getPasswordError('Password@123')).toBeUndefined()
    expect(getPasswordError('Abcdef1!')).toBeUndefined()
  })

  it('accepts exactly 64 characters', () => {
    expect(getPasswordError('Zz9#'.repeat(16))).toBeUndefined()
  })

  it('rejects fewer than 8 characters', () => {
    expect(getPasswordError('Ab1!xyz')).toBe('Password must be at least 8 characters')
  })

  it('rejects more than 64 characters', () => {
    expect(getPasswordError('Zz9#'.repeat(16) + 'A')).toBe('Password must be at most 64 characters')
  })

  it('rejects a password without an uppercase letter', () => {
    expect(getPasswordError('password@123')).toBe('Password must contain at least one uppercase letter')
  })

  it('rejects a password without a lowercase letter', () => {
    expect(getPasswordError('PASSWORD@123')).toBe('Password must contain at least one lowercase letter')
  })

  it('rejects a password without a digit', () => {
    expect(getPasswordError('Password@abc')).toBe('Password must contain at least one digit')
  })

  it('rejects a password without a special character', () => {
    expect(getPasswordError('Password1234')).toBe('Password must contain at least one special character')
  })

  it('rejects a password containing spaces', () => {
    expect(getPasswordError('Pass word@123')).toBe('Password must not contain spaces')
  })

  it('reports the first failed rule', () => {
    expect(getPasswordError('abc')).toBe('Password must be at least 8 characters')
  })
})

describe('PASSWORD_RULES', () => {
  it('marks each rule passed or failed independently', () => {
    const passed = PASSWORD_RULES.filter(r => r.test('password')).map(r => r.id)
    expect(passed).toEqual(['minLength', 'maxLength', 'lowercase', 'noSpaces'])
  })
})
