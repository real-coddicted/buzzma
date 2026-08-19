import { useEffect, useState } from 'react'
import { Card } from '../components/ui/Card'
import { Avatar } from '../components/ui/Avatar'
import { Button } from '../components/ui/Button'
import { Toast } from '../components/ui/Toast'
import { IconChevronLeft } from '../components/ui/icons'
import { fetchUserSettingsById, updateUserSettingsById, FLAG_TO_DTO_KEY, type UserSettingsDto, type UserSettingsFlagDto } from '../api/userSettingsApi'
import type { UserSummaryDto } from '../api/userApi'

interface UserSettingsPageProps {
  user: UserSummaryDto
  onBack: () => void
}

export function UserSettingsPage({ user, onBack }: UserSettingsPageProps) {
  const [values, setValues]     = useState<UserSettingsFlagDto[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving]   = useState(false)
  const [toast, setToast]     = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  useEffect(() => {
    setLoading(true)
    fetchUserSettingsById(user.id!)
      .then(setValues)
      .catch(err => setToast({ message: (err as Error).message, type: 'error' }))
      .finally(() => setLoading(false))
  }, [user.id])

  function toggle(flag: string) {
    setValues(prev => prev ? prev.map(f => f.flag === flag ? { ...f, enabled: !f.enabled } : f) : prev)
  }

  async function handleUpdate() {
    if (!values) return
    setSaving(true)
    try {
      const payload: UserSettingsDto = {}
      for (const f of values) {
        const key = f.flag ? FLAG_TO_DTO_KEY[f.flag] : undefined
        if (key) payload[key] = f.enabled ?? false
      }
      const updated = await updateUserSettingsById(user.id!, payload)
      setValues(updated)
      setToast({ message: 'Settings updated successfully.', type: 'success' })
    } catch (err) {
      setToast({ message: (err as Error).message, type: 'error' })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      {/* Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors"
        >
          <IconChevronLeft size={20} />
        </button>
        <div className="flex items-center gap-3">
          <Avatar name={user.name} src={user.avatar} size="md" />
          <div>
            <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
              {user.name}
            </h1>
            <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
              User Settings
            </p>
          </div>
        </div>
      </div>

      <Card padded={false}>
        {loading && (
          <p className="flex justify-center py-10 text-xs text-ink-light-muted dark:text-ink-dark-muted">
            Loading settings…
          </p>
        )}

        {!loading && !values && (
          <p className="flex justify-center py-10 text-xs text-ink-light-muted dark:text-ink-dark-muted">
            Failed to load settings.
          </p>
        )}

        {values && !loading && (
          <>
            <ul className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
              {values.map(({ flag, enabled, displayName }) => (
                <li key={flag} className="px-4 py-3 flex items-center justify-between gap-4">
                  <label
                    htmlFor={flag}
                    className="text-sm text-ink-light-primary dark:text-ink-dark-primary cursor-pointer select-none"
                  >
                    {displayName}
                  </label>
                  <input
                    id={flag}
                    type="checkbox"
                    checked={enabled ?? false}
                    onChange={() => flag && toggle(flag)}
                    className="w-4 h-4 accent-neon-blue cursor-pointer"
                  />
                </li>
              ))}
            </ul>

            <div className="px-4 py-3 border-t border-surface-light-border dark:border-surface-dark-border flex justify-end">
              <Button variant="pink" size="sm" loading={saving} onClick={handleUpdate}>
                Update
              </Button>
            </div>
          </>
        )}
      </Card>

      {toast && (
        <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />
      )}
    </div>
  )
}