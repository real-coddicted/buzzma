import { describe, it, expect } from 'vitest'
import { readViewAsParam, withViewAsParam, resolveViewAs } from './viewAs'

const USER_ID = '22222222-2222-2222-2222-222222222222'

describe('readViewAsParam', () => {
  it('returns the viewAs user id', () => {
    expect(readViewAsParam(`?view=detail&viewAs=${USER_ID}`)).toBe(USER_ID)
  })

  it('returns null when absent or empty', () => {
    expect(readViewAsParam('?view=detail')).toBeNull()
    expect(readViewAsParam('?viewAs=')).toBeNull()
    expect(readViewAsParam('')).toBeNull()
  })
})

describe('withViewAsParam', () => {
  it('adds viewAs while keeping other params', () => {
    expect(withViewAsParam('?view=detail&id=1', USER_ID)).toBe(`?view=detail&id=1&viewAs=${USER_ID}`)
  })

  it('adds viewAs to an empty query', () => {
    expect(withViewAsParam('', USER_ID)).toBe(`?viewAs=${USER_ID}`)
  })

  it('replaces an existing viewAs', () => {
    expect(withViewAsParam('?viewAs=old', USER_ID)).toBe(`?viewAs=${USER_ID}`)
  })

  it('removes viewAs when the user id is null', () => {
    expect(withViewAsParam(`?view=detail&viewAs=${USER_ID}`, null)).toBe('?view=detail')
    expect(withViewAsParam(`?viewAs=${USER_ID}`, null)).toBe('')
  })
})

describe('resolveViewAs', () => {
  it('keeps the user id for admins', () => {
    expect(resolveViewAs(USER_ID, 'ROLE_ADMIN')).toBe(USER_ID)
  })

  it('ignores it for everyone else', () => {
    expect(resolveViewAs(USER_ID, 'ROLE_AGENCY')).toBeNull()
    expect(resolveViewAs(USER_ID, null)).toBeNull()
  })

  it('returns null when nothing is selected', () => {
    expect(resolveViewAs(null, 'ROLE_ADMIN')).toBeNull()
  })
})
