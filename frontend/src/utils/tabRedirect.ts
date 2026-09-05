import { NAV_ITEMS } from '../config/navItems'
import type { NavPage } from '../types'
import type { components } from '../types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

export const TAB_FLAGS: Partial<Record<NavPage, keyof UserSettingsDto>> = {
  ...Object.fromEntries(NAV_ITEMS.map(item => [item.page, item.flagKey])),
  // 'tickets' is a ticket-detail page, not a sidebar item, so it isn't in NAV_ITEMS.
  tickets: 'ticketsTabEnabled',
}

// Pages that stay reachable even when userSettings fails to load — the
// support/footer NAV_ITEMS plus utility pages that were never gated by a
// tab flag in the first place.
const SUPPORT_PAGES: ReadonlySet<NavPage> = new Set<NavPage>([
  ...NAV_ITEMS.filter(item => item.section === 'footer').map(item => item.page),
  'profile', 'notifications', 'raise-ticket', 'tickets',
])

export function isTabDisabled(page: NavPage, settings: UserSettingsDto): boolean {
  const key = TAB_FLAGS[page]
  return key !== undefined && settings[key] === false
}

/**
 * Single source of truth for "can the user reach this page right now" — used by
 * both Sidebar (what to render) and App (whether to redirect off the current page):
 *   - settings loaded  → whatever the user's tab flags say
 *   - settings missing → only the always-safe support pages
 */
export function isPageVisible(page: NavPage, settings: UserSettingsDto | null): boolean {
  if (!settings) return SUPPORT_PAGES.has(page)
  return !isTabDisabled(page, settings)
}

/** Governs the "Account" sidebar row specifically, since it targets 'profile' — a page with no tab flag of its own. */
export function isSettingsMenuVisible(settings: UserSettingsDto | null): boolean {
  return !settings || settings.settingsTabEnabled !== false
}

// Derived from NAV_ITEMS so the redirect fallback can never drift from the
// sidebar's actual rendering order. 'dashboard' stays excluded — it renders
// static demo data rather than real data, so it's never picked as an
// automatic redirect target even when enabled.
const FALLBACK_ORDER: NavPage[] = NAV_ITEMS.filter(item => item.page !== 'dashboard').map(item => item.page)

export function getFirstEnabledPage(settings: UserSettingsDto): NavPage {
  return FALLBACK_ORDER.find(page => isPageVisible(page, settings)) ?? 'users'
}
