import { describe, it, expect } from 'vitest'
import { isAuthPath } from './postLoginRedirect'

describe('isAuthPath', () => {
  it('returns true for login, register, forgot-password and reset-password', () => {
    expect(isAuthPath('/login')).toBe(true)
    expect(isAuthPath('/register')).toBe(true)
    expect(isAuthPath('/forgot-password')).toBe(true)
    expect(isAuthPath('/reset-password')).toBe(true)
  })

  it('returns false for any other path', () => {
    expect(isAuthPath('/deals')).toBe(false)
    expect(isAuthPath('/')).toBe(false)
    expect(isAuthPath('/dashboard')).toBe(false)
  })
})
