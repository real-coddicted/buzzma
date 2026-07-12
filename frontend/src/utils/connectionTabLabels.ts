import type { components } from '../types/api'

type UserRoleValue = components['schemas']['UserSummary']['role']

export interface ConnectionTabLabels {
  parent: string
  /** Null hides the child tab entirely — this role has no child connections. */
  child: string | null
}

const CONNECTION_TAB_LABELS: Partial<Record<NonNullable<UserRoleValue>, ConnectionTabLabels>> = {
  ROLE_AGENCY:   { parent: 'Admin',    child: 'Mediators' },
  ROLE_MEDIATOR: { parent: 'Agencies', child: 'Buyers' },
  ROLE_BUYER:    { parent: 'Mediators', child: null },
}

const DEFAULT_TAB_LABELS: ConnectionTabLabels = { parent: 'Parent Connections', child: 'Child Connections' }

/** Roles not covered by the explicit mapping (e.g. Admin, Brand) fall back to generic labels. */
export function getConnectionTabLabels(role: UserRoleValue | undefined): ConnectionTabLabels {
  if (!role) return DEFAULT_TAB_LABELS
  return CONNECTION_TAB_LABELS[role] ?? DEFAULT_TAB_LABELS
}