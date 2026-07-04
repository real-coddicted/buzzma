import { useEffect, useState } from 'react'
import { DetailsCard } from '../components/ui/DetailsCard'
import { ChangePasswordCard } from '../components/ui/ChangePasswordCard'
import { BankDetailsForm } from '../components/ui/BankDetailsForm'
import { getCurrentUser } from '../api/client'
import type { CurrentUser } from '../api/client'
import { fetchCurrentUser } from '../api/userApi'
import type { UserDetails } from '../types/ProfileTypes'
import { roleToType } from '../utils/userRole'

type ProfileTab = 'personal' | 'password' | 'banking'

const tabs: { value: ProfileTab; label: string }[] = [
  { value: 'personal',  label: 'Personal'        },
  { value: 'password',  label: 'Change Password'  },
  { value: 'banking',   label: 'Banking Details'  },
]

function toUserDetails(user: CurrentUser): UserDetails {
  return {
    type: roleToType(user.role),
    name: user.name ?? '',
    mobile: user.mobile ?? '',
    email: user.email,
  }
}

export function Profile() {
  const [user, setUser] = useState<CurrentUser | null>(() => getCurrentUser())
  const [activeTab, setActiveTab] = useState<ProfileTab>('personal')

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

      <div className="flex gap-1 p-1 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border self-start mb-6 w-fit">
        {tabs.map(t => (
          <button
            key={t.value}
            onClick={() => setActiveTab(t.value)}
            className={[
              'px-3 py-1.5 rounded-md text-xs font-medium transition-all',
              activeTab === t.value
                ? 'bg-surface-light-card dark:bg-surface-dark-card text-ink-light-primary dark:text-ink-dark-primary shadow-sm'
                : 'text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary',
            ].join(' ')}
          >
            {t.label}
          </button>
        ))}
      </div>

      {activeTab === 'personal' && (
        user ? (
          <DetailsCard details={toUserDetails(user)} />
        ) : (
          <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
            <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
              Unable to load account details. Please sign in again.
            </p>
          </div>
        )
      )}

      {activeTab === 'password' && <ChangePasswordCard />}

      {activeTab === 'banking' && (
        user?.id
          ? <BankDetailsForm userId={user.id} />
          : (
            <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
              <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
                Unable to load banking details. Please sign in again.
              </p>
            </div>
          )
      )}
    </div>
  )
}
