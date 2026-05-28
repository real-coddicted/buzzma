import { UserList } from '../components/ui/users/UserList'
import { mockUsers } from '../data/userMockData'

export function Users() {
  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Users
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          {mockUsers.length} users
        </p>
      </div>

      <UserList users={mockUsers} />
    </div>
  )
}
