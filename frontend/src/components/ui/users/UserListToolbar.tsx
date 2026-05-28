import { SearchInput } from '../SearchInput'
import { StatusFilterPills } from '../StatusFilterPills'
import { USER_FILTER_OPTIONS } from './userConstants'
import type { UserStatus } from '../../../types'

interface UserListToolbarProps {
  search: string
  onSearchChange: (value: string) => void
  statusFilter: UserStatus | 'all'
  onStatusFilterChange: (value: UserStatus | 'all') => void
  searchPlaceholder?: string
}

export function UserListToolbar({
  search,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
  searchPlaceholder = 'Search names…',
}: UserListToolbarProps) {
  return (
    <div className="p-4 flex flex-col gap-3 border-b border-surface-light-border dark:border-surface-dark-border">
      <SearchInput
        value={search}
        onChange={onSearchChange}
        placeholder={searchPlaceholder}
      />
      <StatusFilterPills
        options={USER_FILTER_OPTIONS}
        value={statusFilter}
        onChange={onStatusFilterChange}
      />
    </div>
  )
}
