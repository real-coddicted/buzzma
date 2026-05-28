import type { FilterOption } from '../StatusFilterPills'
import type { StatCardAccent, UserStatus } from '../../../types'

export const USER_STATUS_CONFIG: Record<UserStatus, { label: string; variant: StatCardAccent; dot: string }> = {
  active:   { label: 'Active',   variant: 'green',  dot: 'bg-neon-green' },
  pending:  { label: 'Pending',  variant: 'yellow', dot: 'bg-neon-yellow' },
  inactive: { label: 'Inactive', variant: 'orange', dot: 'bg-neon-orange' },
}

export const USER_FILTER_OPTIONS: FilterOption<UserStatus | 'all'>[] = [
  { value: 'all',      label: 'All',      activeClass: 'bg-neon-blue/10   text-neon-blue   border-neon-blue/30'   },
  { value: 'active',   label: 'Active',   activeClass: 'bg-neon-green/10  text-neon-green  border-neon-green/30'  },
  { value: 'pending',  label: 'Pending',  activeClass: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/30' },
  { value: 'inactive', label: 'Inactive', activeClass: 'bg-neon-orange/10 text-neon-orange border-neon-orange/30' },
]
