import { useState } from 'react'
import { Modal } from '../Modal'

const inputBase =
  'w-full rounded-lg border bg-surface-light-base dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm outline-none transition-colors ' +
  'focus:border-neon-blue focus:ring-1 focus:ring-neon-blue/30'

interface ResetPasswordModalProps {
  onConfirm: (currentPassword: string) => Promise<string>
  onCancel: () => void
}

export function ResetPasswordModal({ onConfirm, onCancel }: ResetPasswordModalProps) {
  const [currentPassword, setCurrentPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [apiError, setApiError] = useState('')
  const [newPassword, setNewPassword] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setApiError('')
    try {
      setNewPassword(await onConfirm(currentPassword))
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Something went wrong. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  function handleCopy() {
    navigator.clipboard.writeText(newPassword ?? '').then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  if (newPassword) {
    return (
      <Modal onClose={onCancel}>
        <h2 className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          Password Reset
        </h2>
        <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary leading-snug">
          The user&rsquo;s password has been reset and they have been signed out everywhere. Share
          this new password with them &mdash; it will not be shown again.
        </p>

        <div className="flex items-center gap-3 rounded-lg bg-surface-light-raised dark:bg-surface-dark-raised border border-surface-light-border dark:border-surface-dark-border px-4 py-3">
          <span className="flex-1 font-mono text-sm tracking-widest text-neon-blue">
            {newPassword}
          </span>
          <button
            onClick={handleCopy}
            className={['text-xs font-semibold transition-colors', copied ? 'text-neon-green' : 'text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-blue'].join(' ')}
          >
            {copied ? 'Copied!' : 'Copy'}
          </button>
        </div>

        <button
          type="button"
          onClick={onCancel}
          className="w-full py-2 text-sm font-semibold rounded-lg bg-surface-light-raised dark:bg-surface-dark-raised text-ink-light-primary dark:text-ink-dark-primary hover:brightness-110 transition-all"
        >
          Done
        </button>
      </Modal>
    )
  }

  return (
    <Modal onClose={busy ? undefined : onCancel}>
      <h2 className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">
        Reset Password
      </h2>
      <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary leading-snug">
        This will reset the user&rsquo;s password to a new randomly generated password and sign
        them out everywhere. Enter your own current password to confirm.
      </p>

      <form onSubmit={handleSubmit} noValidate className="space-y-3">
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            Your Current Password
          </label>
          <input
            type="password"
            placeholder="Your current password"
            value={currentPassword}
            onChange={e => setCurrentPassword(e.target.value)}
            className={inputBase}
          />
        </div>
        {apiError && <p className="text-xs text-neon-red">{apiError}</p>}

        <div className="flex gap-2 pt-1">
          <button
            type="button"
            onClick={onCancel}
            disabled={busy}
            className="flex-1 py-2 text-sm font-semibold rounded-lg bg-surface-light-raised dark:bg-surface-dark-raised text-ink-light-primary dark:text-ink-dark-primary hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={busy || !currentPassword}
            className="flex-1 py-2 text-sm font-semibold rounded-lg bg-neon-red/15 text-neon-red border border-neon-red/40 hover:bg-neon-red/25 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {busy ? 'Working…' : 'Reset Password'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
