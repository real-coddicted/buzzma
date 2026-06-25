import { describe, it, expect } from 'vitest'
import { isTabDisabled, getFirstEnabledPage } from './tabRedirect'
import type { components } from '../types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

const allEnabled: UserSettingsDto = {
  dashboardTabEnabled:   true,
  campaignsTabEnabled:   true,
  connectionsTabEnabled: true,
  assignmentsTabEnabled: true,
  dealTabEnabled:        true,
  claimReviewEnabled:    true,
  ticketsTabEnabled:     true,
  feedbackTabEnabled:    true,
  settingsTabEnabled:    true,
}

describe('isTabDisabled', () => {
  it('returns false when flag is true', () => {
    expect(isTabDisabled('dashboard', { ...allEnabled, dashboardTabEnabled: true })).toBe(false)
  })

  it('returns true when flag is explicitly false', () => {
    expect(isTabDisabled('dashboard', { ...allEnabled, dashboardTabEnabled: false })).toBe(true)
  })

  it('returns false when flag is undefined (not set)', () => {
    const settings: UserSettingsDto = { ...allEnabled, dashboardTabEnabled: undefined }
    expect(isTabDisabled('dashboard', settings)).toBe(false)
  })

  it('returns false for pages with no flag (users, profile, etc.)', () => {
    expect(isTabDisabled('users', allEnabled)).toBe(false)
    expect(isTabDisabled('profile', allEnabled)).toBe(false)
    expect(isTabDisabled('notifications', allEnabled)).toBe(false)
  })
})

describe('getFirstEnabledPage', () => {
  it('returns campaigns when all are enabled (first in fallback order)', () => {
    expect(getFirstEnabledPage(allEnabled)).toBe('campaigns')
  })

  it('skips dashboard (not in fallback order) and returns campaigns', () => {
    const settings = { ...allEnabled, dashboardTabEnabled: false }
    expect(getFirstEnabledPage(settings)).toBe('campaigns')
  })

  it('skips campaigns when disabled, returns connections', () => {
    const settings = { ...allEnabled, campaignsTabEnabled: false }
    expect(getFirstEnabledPage(settings)).toBe('connections')
  })

  it('returns feedback when all other optional pages are disabled', () => {
    const settings: UserSettingsDto = {
      ...allEnabled,
      campaignsTabEnabled:   false,
      connectionsTabEnabled: false,
      assignmentsTabEnabled: false,
      dealTabEnabled:        false,
      claimReviewEnabled:    false,
      ticketsTabEnabled:     false,
    }
    expect(getFirstEnabledPage(settings)).toBe('feedback')
  })

  it('falls back to users when all optional pages are disabled', () => {
    const settings: UserSettingsDto = {
      ...allEnabled,
      campaignsTabEnabled:   false,
      connectionsTabEnabled: false,
      assignmentsTabEnabled: false,
      dealTabEnabled:        false,
      claimReviewEnabled:    false,
      ticketsTabEnabled:     false,
      feedbackTabEnabled:    false,
    }
    expect(getFirstEnabledPage(settings)).toBe('users')
  })
})
