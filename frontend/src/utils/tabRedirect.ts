import { NAV_ITEMS } from '../config/navItems'
import type { NavPage } from '../types'
import type { components } from '../types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

export const TAB_FLAGS: Partial<Record<NavPage, keyof UserSettingsDto>> = {
  ...Object.fromEntries(NAV_ITEMS.map(item => [item.page, item.flagKey])),
  // 'tickets' is a ticket-detail page, not a sidebar item, so it isn't in NAV_ITEMS.
  tickets: 'ticketsTabEnabled',
}

// Derived from NAV_ITEMS so the redirect fallback can never drift from the
// sidebar's actual rendering order. 'dashboard' stays excluded — the redirect
// only ever runs after 'dashboard' is confirmed disabled, so including it would be a no-op.
const FALLBACK_ORDER: NavPage[] = NAV_ITEMS.filter(item => item.page !== 'dashboard').map(item => item.page)

export function isTabDisabled(page: NavPage, settings: UserSettingsDto): boolean {
  const key = TAB_FLAGS[page]
  return key !== undefined && settings[key] === false
}

export function getFirstEnabledPage(settings: UserSettingsDto): NavPage {
  return FALLBACK_ORDER.find(p => !isTabDisabled(p, settings)) ?? 'users'
}