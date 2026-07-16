import { useState, useEffect } from 'react'
import { LabeledField } from './LabeledField'
import { CopyableCode } from './CopyableCode'
import { Button } from './Button'
import { Toast } from './Toast'
import type { UserDetails } from '../../types/ProfileTypes'

interface DetailsCardProps {
  details: UserDetails
  onSave?: (email: string) => Promise<void>
}

const nameLabels: Record<string, string> = {
  brand:    'Brand Name',
  agency:   'Agency Name',
  mediator: 'Mediator Name',
  buyer:    'Buyer Name',
}

const inputBase =
  'w-full rounded-lg border bg-surface-light-base dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm outline-none transition-colors ' +
  'focus:border-neon-blue focus:ring-1 focus:ring-neon-blue/30'

export function DetailsCard({ details, onSave }: DetailsCardProps) {
  const { code, type, name, mobile, email } = details
  const [emailValue, setEmailValue] = useState(email ?? '')
  const [emailError, setEmailError] = useState('')
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState(false)
  const [apiError, setApiError] = useState('')

  useEffect(() => { setEmailValue(email ?? '') }, [email])

  function validateEmail(): boolean {
    const trimmed = emailValue.trim()
    if (!trimmed) {
      setEmailError('Email is required')
      return false
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
      setEmailError('Enter a valid email address')
      return false
    }
    setEmailError('')
    return true
  }

  async function handleSaveEmail(e: React.FormEvent) {
    e.preventDefault()
    if (!onSave || !validateEmail()) return
    setSaving(true)
    setApiError('')
    setSuccess(false)
    try {
      await onSave(emailValue.trim())
      setSuccess(true)
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Something went wrong. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
      <div className="flex items-center justify-between mb-5">
        <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          Account Details
        </h3>
        {type && (
          <span className="text-xs font-medium px-2.5 py-1 rounded-full border border-neon-blue/30 bg-neon-blue/10 text-neon-blue capitalize">
            {type}
          </span>
        )}
      </div>

      <div className="space-y-3">
        {code && (
          <>
            <div className="flex flex-col gap-1">
              <span className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
                Code
              </span>
              <CopyableCode code={code} />
            </div>
            <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          </>
        )}
        <LabeledField
          label={type ? (nameLabels[type] ?? 'Name') : 'Name'}
          value={name}
        />
        <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
        <LabeledField label="Mobile" value={mobile} />
        <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
        {onSave ? (
          <form onSubmit={handleSaveEmail} className="space-y-2">
            <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
              Email
            </label>
            <input
              type="email"
              placeholder="you@example.com"
              value={emailValue}
              onChange={e => { setEmailValue(e.target.value); setEmailError('') }}
              className={inputBase}
            />
            {emailError && <p className="text-xs text-neon-red">{emailError}</p>}
            {apiError && <p className="text-xs text-neon-red">{apiError}</p>}
            {success && (
              <Toast
                message="Email updated successfully."
                onDismiss={() => setSuccess(false)}
              />
            )}
            <div className="flex justify-end pt-1">
              <Button type="submit" variant="primary" loading={saving}>
                Save
              </Button>
            </div>
          </form>
        ) : (
          <LabeledField label="Email" value={email ?? ''} />
        )}
      </div>
    </div>
  )
}
