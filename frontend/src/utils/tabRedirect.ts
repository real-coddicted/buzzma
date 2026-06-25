import type { NavPage } from '../types'
import type { components } from '../types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

export const TAB_FLAGS: Partial<Record<NavPage, keyof UserSettingsDto>> = {
  dashboard:      'dashboardTabEnabled',
  campaigns:      'campaignsTabEnabled',
  connections:    'connectionsTabEnabled',
  assignments:    'assignmentsTabEnabled',
  deals:          'dealTabEnabled',
  'claim-review': 'claimReviewEnabled',
  'my-tickets':   'ticketsTabEnabled',
  tickets:        'ticketsTabEnabled',
  feedback:       'feedbackTabEnabled',
}

const FALLBACK_ORDER: NavPage[] = [
  'campaigns', 'connections', 'assignments', 'deals', 'claim-review', 'my-tickets', 'feedback', 'users',
]

export function isTabDisabled(page: NavPage, settings: UserSettingsDto): boolean {
  const key = TAB_FLAGS[page]
  return key !== undefined && settings[key] === false
}

export function getFirstEnabledPage(settings: UserSettingsDto): NavPage {
  return FALLBACK_ORDER.find(p => !isTabDisabled(p, settings)) ?? 'users'
}