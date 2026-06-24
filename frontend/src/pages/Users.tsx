import { useState } from 'react'
import { UserList } from '../components/ui/users/UserList'
import { UserSettingsPage } from './UserSettingsPage'
import type { UserSummaryDto } from '../api/userApi'

export function Users() {
  const [selectedUser, setSelectedUser] = useState<UserSummaryDto | null>(null)

  if (selectedUser) {
    return (
      <UserSettingsPage
        user={selectedUser}
        onBack={() => setSelectedUser(null)}
      />
    )
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Users
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          Search for a user by mobile number.
        </p>
      </div>

      <UserList onUserClick={setSelectedUser} />
    </div>
  )
}