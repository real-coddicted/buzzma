import { describe, it, expect } from 'vitest'
import { formatDateTime } from './time'

describe('formatDateTime', () => {
  it('includes both date and time', () => {
    const result = formatDateTime('2026-07-07T14:32:00.000Z')
    expect(result).toMatch(/^\d{1,2} \w{3} \d{4}, \d{2}:\d{2}/)
  })

  it('formats the date part as day, short month, year', () => {
    const result = formatDateTime('2026-01-05T00:00:00.000Z')
    expect(result).toMatch(/Jan/)
    expect(result).toMatch(/2026/)
  })

  it('produces a different string than a date-only format for the same input', () => {
    const iso = '2026-07-07T14:32:00.000Z'
    const dateOnly = new Date(iso).toLocaleDateString('en-IN', {
      day: 'numeric', month: 'short', year: 'numeric',
    })
    expect(formatDateTime(iso)).not.toBe(dateOnly)
    expect(formatDateTime(iso)).toContain(dateOnly)
  })
})