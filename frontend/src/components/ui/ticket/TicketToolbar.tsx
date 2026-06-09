import { SearchInput } from '../SearchInput'
import { TicketFilterBar } from './TicketFilterBar'
import type { TicketStatus } from '../../../types/TicketTypes'

interface Props {
  search: string
  onSearchChange: (value: string) => void
  filter: TicketStatus | 'all'
  onFilterChange: (value: TicketStatus | 'all') => void
}

export function TicketToolbar({ search, onSearchChange, filter, onFilterChange }: Props) {
  return (
    <div className="p-4 border-b border-surface-light-border dark:border-surface-dark-border flex flex-wrap items-center gap-3">
      <SearchInput value={search} onChange={onSearchChange} placeholder="Search tickets…" />
      <div className="ml-auto">
        <TicketFilterBar value={filter} onChange={onFilterChange} />
      </div>
    </div>
  )
}
