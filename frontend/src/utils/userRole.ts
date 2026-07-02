import type { UserDetails } from '../types/ProfileTypes'

type Role = 'ROLE_BUYER' | 'ROLE_MEDIATOR' | 'ROLE_AGENCY' | 'ROLE_BRAND' | 'ROLE_ADMIN'

const roleTypeMap: Record<Role, UserDetails['type']> = {
  ROLE_BRAND:    'brand',
  ROLE_AGENCY:   'agency',
  ROLE_MEDIATOR: 'mediator',
  ROLE_BUYER:    'buyer',
  ROLE_ADMIN:    'admin',
}

export function roleToType(role: Role | null | undefined): UserDetails['type'] {
  return role ? roleTypeMap[role] : 'invalid'
}
