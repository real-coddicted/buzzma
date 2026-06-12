import { useState } from 'react'
import { Button } from './Button'
import { Toast } from './Toast'
import { changePassword } from '../../api/authApi'

const inputBase =
  'w-full rounded-lg border bg-surface-light-base dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm outline-none transition-colors ' +
  'focus:border-neon-blue focus:ring-1 focus:ring-neon-blue/30'

type FormErrors = {
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}

export function ChangePasswordCard() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState<FormErrors>({})
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [apiError, setApiError] = useState('')

  function validate(): boolean {
    const next: FormErrors = {}
    if (!currentPassword) next.currentPassword = 'Current password is required'
    if (!newPassword) next.newPassword = 'New password is required'
    else if (newPassword.length < 8) next.newPassword = 'Password must be at least 8 characters'
    if (!confirmPassword) next.confirmPassword = 'Please confirm your password'
    else if (newPassword !== confirmPassword) next.confirmPassword = 'Passwords do not match'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    setApiError('')
    setSuccess(false)
    try {
      await changePassword(currentPassword, newPassword)
      setSuccess(true)
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
      <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-5">
        Change Password
      </h3>
      <form onSubmit={handleSubmit} noValidate className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            Current Password
          </label>
          <input
            type="password"
            placeholder="Your current password"
            value={currentPassword}
            onChange={e => { setCurrentPassword(e.target.value); setErrors(p => ({ ...p, currentPassword: undefined })) }}
            className={inputBase}
          />
          {errors.currentPassword && (
            <p className="mt-1 text-xs text-neon-red">{errors.currentPassword}</p>
          )}
        </div>
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            New Password
          </label>
          <input
            type="password"
            placeholder="Min. 8 characters"
            value={newPassword}
            onChange={e => { setNewPassword(e.target.value); setErrors(p => ({ ...p, newPassword: undefined })) }}
            className={inputBase}
          />
          {errors.newPassword && (
            <p className="mt-1 text-xs text-neon-red">{errors.newPassword}</p>
          )}
        </div>
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            Confirm Password
          </label>
          <input
            type="password"
            placeholder="Repeat new password"
            value={confirmPassword}
            onChange={e => { setConfirmPassword(e.target.value); setErrors(p => ({ ...p, confirmPassword: undefined })) }}
            className={inputBase}
          />
          {errors.confirmPassword && (
            <p className="mt-1 text-xs text-neon-red">{errors.confirmPassword}</p>
          )}
        </div>
        {apiError && <p className="text-xs text-neon-red">{apiError}</p>}
        {success && (
          <Toast
            message="Password updated successfully."
            onDismiss={() => setSuccess(false)}
          />
        )}
        <div className="flex justify-end">
          <Button type="submit" variant="primary" loading={loading}>
            Update Password
          </Button>
        </div>
      </form>
    </div>
  )
}
