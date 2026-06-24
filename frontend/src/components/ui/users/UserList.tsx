import { useState } from 'react'
import { Card } from '../Card'
import { Toast } from '../Toast'
import { UserListToolbar } from './UserListToolbar'
import { UserListItem } from './UserListItem'
import { searchUserByMobile, type UserSummaryDto } from '../../../api/userApi'

interface UserListProps {
  onUserClick?: (user: UserSummaryDto) => void
}

type SearchState =
  | { kind: 'idle' }
  | { kind: 'loading' }
  | { kind: 'result'; user: UserSummaryDto }

export function UserList({ onUserClick }: UserListProps) {
  const [search, setSearch] = useState('')
  const [state, setState]   = useState<SearchState>({ kind: 'idle' })
  const [error, setError]   = useState<string | null>(null)

  async function handleSubmit() {
    const mobile = search.trim()
    if (!mobile) return
    setState({ kind: 'loading' })
    try {
      const user = await searchUserByMobile(mobile)
      setState({ kind: 'result', user })
    } catch (err) {
      setState({ kind: 'idle' })
      setError((err as Error).message)
    }
  }

  return (
    <>
    <Card padded={false}>
      <UserListToolbar
        search={search}
        onSearchChange={value => { setSearch(value); setState({ kind: 'idle' }) }}
        onSubmit={handleSubmit}
      />

      <div className="min-h-[80px]">
        {state.kind === 'idle' && (
          <p className="flex justify-center py-10 text-xs text-ink-light-muted dark:text-ink-dark-muted">
            Enter a mobile number to search.
          </p>
        )}

        {state.kind === 'loading' && (
          <p className="flex justify-center py-10 text-xs text-ink-light-muted dark:text-ink-dark-muted">
            Searching…
          </p>
        )}

        {state.kind === 'result' && (
          <ul>
            <UserListItem
              user={state.user}
              onClick={onUserClick && state.user.id ? () => onUserClick(state.user) : undefined}
            />
          </ul>
        )}
      </div>
    </Card>

      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </>
  )
}