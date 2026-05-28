import { useMemo, useState } from 'react'
import { Card } from '../Card'
import { PaginationToolbar } from '../PaginationToolbar'
import { UserListToolbar } from './UserListToolbar'
import { UserListItem } from './UserListItem'
import type { User, UserStatus } from '../../../types'

interface UserListProps {
  users: User[]
  pageSize?: number
  searchPlaceholder?: string
}

export function UserList({
  users,
  pageSize = 10,
  searchPlaceholder = 'Search names…',
}: UserListProps) {
  const [search, setSearch]             = useState('')
  const [statusFilter, setStatusFilter] = useState<UserStatus | 'all'>('all')
  const [currentPage, setCurrentPage]   = useState(1)

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    return users.filter(u => {
      const matchesSearch = !q || u.name.toLowerCase().includes(q)
      const matchesStatus = statusFilter === 'all' || u.status === statusFilter
      return matchesSearch && matchesStatus
    })
  }, [users, search, statusFilter])

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  // Derive the in-range page so a shrinking dataset (e.g. search filter) never
  // shows an empty slice for a frame before an effect can clamp state.
  const safePage = Math.min(currentPage, totalPages)

  const visible = useMemo(() => {
    const start = (safePage - 1) * pageSize
    return filtered.slice(start, start + pageSize)
  }, [filtered, safePage, pageSize])

  const isFiltering = search.trim() !== '' || statusFilter !== 'all'

  return (
    <Card padded={false}>
      <UserListToolbar
        search={search}
        onSearchChange={setSearch}
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        searchPlaceholder={searchPlaceholder}
      />

      <div className="max-h-[420px] overflow-y-auto">
        {visible.length === 0 ? (
          <div className="flex justify-center py-20 text-sm text-ink-light-muted dark:text-ink-dark-muted">
            {isFiltering ? 'No users match your filters.' : 'No users yet.'}
          </div>
        ) : (
          <ul className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {visible.map((user, i) => (
              <UserListItem
                key={`${(safePage - 1) * pageSize + i}-${user.name}`}
                user={user}
              />
            ))}
          </ul>
        )}
      </div>

      <PaginationToolbar
        currentPage={safePage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </Card>
  )
}
