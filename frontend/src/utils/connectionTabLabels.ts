import type { components } from '../types/api'

type UserRoleValue = components['schemas']['UserSummary']['role']

export interface ConnectionBucket {
  label: string
  /** Only connections whose other-party role matches this are shown in the bucket. Undefined shows all roles. */
  role?: NonNullable<UserRoleValue>
}

export interface ConnectionTabLabels {
  /** Null hides the parent tab entirely — this role has no parent connections. */
  parent: ConnectionBucket | null
  /** Null hides the child tab entirely — this role has no child connections. */
  child: ConnectionBucket | null
}

const CONNECTION_TAB_LABELS: Partial<Record<NonNullable<UserRoleValue>, ConnectionTabLabels>> = {
  ROLE_BUYER:    { parent: { label: 'Mediators', role: 'ROLE_MEDIATOR' }, child: null },
  ROLE_MEDIATOR: { parent: { label: 'Agencies', role: 'ROLE_AGENCY' },  child: { label: 'Buyers', role: 'ROLE_BUYER' } },
  ROLE_AGENCY:   { parent: { label: 'Brand', role: 'ROLE_BRAND' },      child: { label: 'Mediators', role: 'ROLE_MEDIATOR' } },
  ROLE_BRAND:    { parent: null, child: { label: 'Agencies', role: 'ROLE_AGENCY' } },
}

const DEFAULT_TAB_LABELS: ConnectionTabLabels = {
  parent: { label: 'Parent Connections' },
  child: { label: 'Child Connections' },
}

/** Roles not covered by the explicit mapping (e.g. Admin) fall back to generic, unfiltered labels. */
export function getConnectionTabLabels(role: UserRoleValue | undefined): ConnectionTabLabels {
  if (!role) return DEFAULT_TAB_LABELS
  return CONNECTION_TAB_LABELS[role] ?? DEFAULT_TAB_LABELS
}
