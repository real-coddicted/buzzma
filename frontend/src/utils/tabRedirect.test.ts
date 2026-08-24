import { describe, it, expect } from 'vitest'
import { isTabDisabled, getFirstEnabledPage } from './tabRedirect'
import { NAV_ITEMS } from '../config/navItems'
import type { components } from '../types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

const allEnabled: UserSettingsDto = {
  dashboardTabEnabled:   true,
  campaignsTabEnabled:   true,
  connectionsTabEnabled: true,
  assignmentsTabEnabled: true,
  dealTabEnabled:        true,
  myClaimsTabEnabled:    true,
  claimReviewEnabled:    true,
  ticketsTabEnabled:     true,
  feedbackTabEnabled:    true,
  settingsTabEnabled:    true,
  usersTabEnabled:       true,
  myPaymentsTabEnabled:  true,
  userPayoutsTabEnabled: true,
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

  it('returns false for pages with no flag (profile, notifications, etc.)', () => {
    expect(isTabDisabled('profile', allEnabled)).toBe(false)
    expect(isTabDisabled('notifications', allEnabled)).toBe(false)
  })

  it('returns false for users when usersTabEnabled is true', () => {
    expect(isTabDisabled('users', { ...allEnabled, usersTabEnabled: true })).toBe(false)
  })

  it('returns true for users when usersTabEnabled is false', () => {
    expect(isTabDisabled('users', { ...allEnabled, usersTabEnabled: false })).toBe(true)
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

  it('skips campaigns when disabled, returns assignments', () => {
    const settings = { ...allEnabled, campaignsTabEnabled: false }
    expect(getFirstEnabledPage(settings)).toBe('assignments')
  })

  it('returns feedback when all other optional pages are disabled', () => {
    const settings: UserSettingsDto = {
      ...allEnabled,
      campaignsTabEnabled:   false,
      connectionsTabEnabled: false,
      assignmentsTabEnabled: false,
      dealTabEnabled:        false,
      myClaimsTabEnabled:    false,
      claimReviewEnabled:    false,
      myPaymentsTabEnabled:  false,
      userPayoutsTabEnabled: false,
      usersTabEnabled:       false,
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
      myClaimsTabEnabled:    false,
      claimReviewEnabled:    false,
      myPaymentsTabEnabled:  false,
      userPayoutsTabEnabled: false,
      ticketsTabEnabled:     false,
      feedbackTabEnabled:    false,
    }
    expect(getFirstEnabledPage(settings)).toBe('users')
  })

  it('returns assignments for a mediator (campaigns disabled, assignments/connections enabled)', () => {
    const settings: UserSettingsDto = {
      ...allEnabled,
      campaignsTabEnabled: false,
      assignmentsTabEnabled: true,
      connectionsTabEnabled: true,
    }
    expect(getFirstEnabledPage(settings)).toBe('assignments')
  })

  it('returns deals for a buyer (campaigns/assignments disabled, connections/deals enabled)', () => {
    const settings: UserSettingsDto = {
      ...allEnabled,
      campaignsTabEnabled: false,
      assignmentsTabEnabled: false,
      connectionsTabEnabled: true,
      dealTabEnabled: true,
    }
    expect(getFirstEnabledPage(settings)).toBe('deals')
  })

  it('stays in sync with the sidebar rendering order in NAV_ITEMS', () => {
    const expectedOrder = NAV_ITEMS.filter(item => item.page !== 'dashboard').map(item => item.page)
    const settings = { ...allEnabled }
    for (const page of expectedOrder) {
      expect(getFirstEnabledPage(settings)).toBe(page)
      const key = NAV_ITEMS.find(item => item.page === page)!.flagKey
      settings[key] = false
    }
  })
})
