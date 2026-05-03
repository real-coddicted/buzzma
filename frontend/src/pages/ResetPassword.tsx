import { useState } from 'react'
import { Button } from '../components/ui/Button'
import type { ResetPasswordForm } from '../types/ForgotPasswordTypes'

interface ResetPasswordProps {
  onSuccess: () => void
  onGoToLogin: () => void
}

const inputBase =
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm outline-none transition-colors ' +
  'focus:border-neon-blue dark:focus:border-neon-blue focus:ring-1 focus:ring-neon-blue/30'

function PasswordInput({
  placeholder,
  value,
  onChange,
  error,
}: {
  placeholder: string
  value: string
  onChange: (v: string) => void
  error?: string
}) {
  const [show, setShow] = useState(false)
  return (
    <div>
      <div className="relative">
        <input
          type={show ? 'text' : 'password'}
          placeholder={placeholder}
          value={value}
          onChange={e => onChange(e.target.value)}
          className={inputBase + ' pr-10'}
        />
        <button
          type="button"
          onClick={() => setShow(p => !p)}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors"
          tabIndex={-1}
        >
          {show ? (
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
            </svg>
          ) : (
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          )}
        </button>
      </div>
      {error && <p className="mt-1 text-xs text-neon-red">{error}</p>}
    </div>
  )
}

export function ResetPassword({ onSuccess, onGoToLogin }: ResetPasswordProps) {
  const [form, setForm] = useState<ResetPasswordForm>({
    password: '',
    confirmPassword: '',
  })
  const [errors, setErrors] = useState<Partial<Record<keyof ResetPasswordForm, string>>>({})

  function set<K extends keyof ResetPasswordForm>(key: K, value: string) {
    setForm(prev => ({ ...prev, [key]: value }))
    setErrors(prev => ({ ...prev, [key]: undefined }))
  }

  function validate(): boolean {
    const next: Partial<Record<keyof ResetPasswordForm, string>> = {}
    if (!form.password) next.password = 'Password is required'
    else if (form.password.length < 8) next.password = 'Minimum 8 characters'
    if (!form.confirmPassword) next.confirmPassword = 'Please confirm your password'
    else if (form.password !== form.confirmPassword) next.confirmPassword = 'Passwords do not match'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (validate()) onSuccess()
  }

  return (
    <div className="min-h-screen bg-surface-light-base dark:bg-surface-dark-base flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <span className="text-2xl font-bold text-neon-blue tracking-tight">Buzzma</span>
          <p className="mt-1 text-sm text-ink-light-muted dark:text-ink-dark-muted">
            Set a new password
          </p>
        </div>

        <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-6">
          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                New Password
              </label>
              <PasswordInput
                placeholder="Min. 8 characters"
                value={form.password}
                onChange={v => set('password', v)}
                error={errors.password}
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Confirm Password
              </label>
              <PasswordInput
                placeholder="Re-enter your password"
                value={form.confirmPassword}
                onChange={v => set('confirmPassword', v)}
                error={errors.confirmPassword}
              />
            </div>

            <Button type="submit" variant="primary" size="lg" className="w-full mt-2">
              Reset Password
            </Button>
          </form>
        </div>

        <p className="text-center text-xs text-ink-light-muted dark:text-ink-dark-muted mt-6">
          Remember your password?{' '}
          <button
            type="button"
            onClick={onGoToLogin}
            className="text-neon-blue hover:underline font-medium"
          >
            Sign in
          </button>
        </p>
      </div>
    </div>
  )
}
