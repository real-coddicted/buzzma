import { Button } from '../Button'
import { SearchInput } from '../SearchInput'

interface UserListToolbarProps {
  search: string
  onSearchChange: (value: string) => void
  onSubmit: () => void
  searchPlaceholder?: string
}

export function UserListToolbar({
  search,
  onSearchChange,
  onSubmit,
  searchPlaceholder = 'Search by mobile number…',
}: UserListToolbarProps) {
  return (
    <div className="p-4 border-b border-surface-light-border dark:border-surface-dark-border">
      <form
        className="flex gap-2"
        onSubmit={e => { e.preventDefault(); onSubmit() }}
      >
        <SearchInput
          value={search}
          onChange={onSearchChange}
          placeholder={searchPlaceholder}
        />
        <Button type="submit" variant="pink" size="sm" disabled={search.trim() === ''}>
          Search
        </Button>
      </form>
    </div>
  )
}