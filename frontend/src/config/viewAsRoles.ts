import type { NavPage } from '../types'

export type ViewAsRole = 'ROLE_BRAND' | 'ROLE_AGENCY' | 'ROLE_MEDIATOR' | 'ROLE_BUYER'

const ALL_ROLES: ViewAsRole[] = ['ROLE_BRAND', 'ROLE_AGENCY', 'ROLE_MEDIATOR', 'ROLE_BUYER']

// Roles an admin can view each page as: the roles whose default tab settings show the page
// (SettingsUtils on the backend). My Payments and User Payouts are off by default for every
// role, so they use the roles their endpoints allow. An empty list hides the picker (admin-only
// pages and the static dashboard).
export const VIEW_AS_ROLES: Record<NavPage, ViewAsRole[]> = {
  dashboard:       [],
  campaigns:       ['ROLE_AGENCY'],
  assignments:     ['ROLE_AGENCY', 'ROLE_MEDIATOR'],
  deals:           ['ROLE_BUYER'],
  'my-claims':     ['ROLE_BUYER'],
  'claim-review':  ['ROLE_BRAND', 'ROLE_AGENCY', 'ROLE_MEDIATOR'],
  'my-payments':   ['ROLE_MEDIATOR', 'ROLE_BUYER'],
  'user-payouts':  ['ROLE_AGENCY', 'ROLE_MEDIATOR'],
  connections:     ALL_ROLES,
  users:           [],
  tickets:         [],
  'my-tickets':    ALL_ROLES,
  feedback:        ALL_ROLES,
  profile:         ALL_ROLES,
  'raise-ticket':  ALL_ROLES,
  notifications:   ALL_ROLES,
  faq:             [],
}
