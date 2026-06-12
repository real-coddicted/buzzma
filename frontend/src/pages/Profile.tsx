import { useEffect, useState } from 'react'
import { DetailsCard } from '../components/ui/DetailsCard'
import { ChangePasswordCard } from '../components/ui/ChangePasswordCard'
import { getCurrentUser } from '../api/client'
import type { CurrentUser } from '../api/client'
import { fetchCurrentUser } from '../api/userApi'
import type { UserDetails } from '../types/ProfileTypes'

const roleTypeMap: Record<NonNullable<CurrentUser['role']>, UserDetails['type']> = {
  ROLE_BRAND:    'brand',
  ROLE_AGENCY:   'agency',
  ROLE_MEDIATOR: 'mediator',
  ROLE_BUYER:    'buyer',
  ROLE_ADMIN:    'admin',
}

function toUserDetails(user: CurrentUser): UserDetails {
  return {
    type: user.role ? roleTypeMap[user.role] : 'buyer',
    name: user.name ?? '',
    mobile: user.mobile ?? '',
    email: user.email,
  }
}

export function Profile() {
  // Render the cached sign-in summary immediately, then refresh from the backend
  const [user, setUser] = useState<CurrentUser | null>(() => getCurrentUser())

  useEffect(() => {
    fetchCurrentUser()
      .then(setUser)
      .catch(() => {
        // Keep showing the cached summary; fetchWithAuth already handles expired sessions
      })
  }, [])

  return (
    <div className="max-w-7xl mx-auto">
      <h1 className="text-lg font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-6">
        Profile
      </h1>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {user ? (
          <DetailsCard details={toUserDetails(user)} />
        ) : (
          <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
            <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
              Unable to load account details. Please sign in again.
            </p>
          </div>
        )}
        <ChangePasswordCard />
      </div>
    </div>
  )
}