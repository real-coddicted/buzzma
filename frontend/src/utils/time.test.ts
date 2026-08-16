import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { formatDateTime, toRelativeTime, formatShortDate } from './time'

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

describe('toRelativeTime', () => {
  const NOW = new Date('2026-08-16T12:00:00.000Z')

  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(NOW)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  function isoMinutesAgo(mins: number): string {
    return new Date(NOW.getTime() - mins * 60_000).toISOString()
  }

  it('returns "just now" for less than a minute ago', () => {
    expect(toRelativeTime(isoMinutesAgo(0))).toBe('just now')
  })

  it('returns minutes ago for under an hour', () => {
    expect(toRelativeTime(isoMinutesAgo(5))).toBe('5m ago')
  })

  it('returns hours ago for under a day', () => {
    expect(toRelativeTime(isoMinutesAgo(3 * 60))).toBe('3h ago')
  })

  it('returns days ago for under 7 days', () => {
    expect(toRelativeTime(isoMinutesAgo(3 * 24 * 60))).toBe('3d ago')
  })

  it('falls back to an absolute date at exactly 7 days', () => {
    const iso = isoMinutesAgo(7 * 24 * 60)
    expect(toRelativeTime(iso)).toBe(formatShortDate(iso))
  })

  it('falls back to an absolute date for a very old timestamp instead of an absurd day count', () => {
    const veryOld = '1970-01-02T00:00:00.000Z'
    const result = toRelativeTime(veryOld)
    expect(result).toBe(formatShortDate(veryOld))
    expect(result).not.toMatch(/d ago/)
  })
})