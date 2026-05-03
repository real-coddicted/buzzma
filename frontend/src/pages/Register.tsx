import { useState, useEffect } from 'react'
import { Button } from '../components/ui/Button'
import { Toast } from '../components/ui/Toast'
import { fetchSecurityQuestions, registerUser } from '../api/authApi'
import type { LoginAs, RegisterForm } from '../types/RegisterTypes'

interface RegisterProps {
  onRegister: () => void
  onGoToLogin: () => void
}

const inputBase =
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm outline-none transition-colors ' +
  'focus:border-neon-blue dark:focus:border-neon-blue focus:ring-1 focus:ring-neon-blue/30'

export function Register({ onRegister, onGoToLogin }: RegisterProps) {
  const [questions, setQuestions] = useState<string[]>([])
  const [form, setForm] = useState<RegisterForm>({
    registerAs: 'brand',
    mobile: '',
    password: '',
    inviteCode: '',
    brandName: '',
    agencyName: '',
    securityQuestion1: '',
    securityAnswer1: '',
    securityQuestion2: '',
    securityAnswer2: '',
  })
  const [showPassword, setShowPassword] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)
  const [showToast, setShowToast] = useState(false)
  const [errors, setErrors] = useState<Partial<Record<keyof RegisterForm, string>>>({})

  useEffect(() => {
    fetchSecurityQuestions().then(qs => {
      setQuestions(qs)
      setForm(prev => ({
        ...prev,
        securityQuestion1: qs[0] ?? '',
        securityQuestion2: qs[1] ?? '',
      }))
    })
  }, [])

  function set<K extends keyof RegisterForm>(key: K, value: RegisterForm[K]) {
    setForm(prev => ({ ...prev, [key]: value }))
    setErrors(prev => ({ ...prev, [key]: undefined }))
  }

  function validate(): boolean {
    const next: Partial<Record<keyof RegisterForm, string>> = {}
    if (!form.mobile.trim()) next.mobile = 'Mobile number is required'
    else if (!/^\+?[0-9]{7,15}$/.test(form.mobile.replace(/\s/g, '')))
      next.mobile = 'Enter a valid mobile number'
    if (!form.password) next.password = 'Password is required'
    else if (form.password.length < 8) next.password = 'Minimum 8 characters'
    if (!form.inviteCode.trim()) next.inviteCode = 'Invite code is required'
    if (form.registerAs === 'brand' && !form.brandName.trim())
      next.brandName = 'Brand name is required'
    if (form.registerAs === 'agency' && !form.agencyName.trim())
      next.agencyName = 'Agency name is required'
    if (!form.securityAnswer1.trim()) next.securityAnswer1 = 'Answer is required'
    if (!form.securityAnswer2.trim()) next.securityAnswer2 = 'Answer is required'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return
    setSubmitting(true)
    setApiError(null)
    try {
      const res = await registerUser(form)
      if (res.success) {
        setShowToast(true)
      } else {
        setApiError(res.message)
      }
    } catch {
      setApiError('Something went wrong. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
    {showToast && (
      <Toast
        message="Registration successful! You can now sign in."
        type="success"
        onDismiss={onRegister}
      />
    )}
    <div className="min-h-screen bg-surface-light-base dark:bg-surface-dark-base flex items-center justify-center p-4 py-10">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <span className="text-2xl font-bold text-neon-blue tracking-tight">Buzzma</span>
          <p className="mt-1 text-sm text-ink-light-muted dark:text-ink-dark-muted">
            Create your account
          </p>
        </div>

        <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-6">
          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            {/* Register As */}
            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Register As
              </label>
              <select
                value={form.registerAs}
                onChange={e => set('registerAs', e.target.value as LoginAs)}
                className={inputBase + ' cursor-pointer'}
              >
                <option value="brand">Brand</option>
                <option value="agency">Agency</option>
              </select>
            </div>

            {/* Conditional name field */}
            {form.registerAs === 'brand' ? (
              <div>
                <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                  Brand Name
                </label>
                <input
                  type="text"
                  placeholder="e.g. Acme Corp"
                  value={form.brandName}
                  onChange={e => set('brandName', e.target.value)}
                  className={inputBase}
                />
                {errors.brandName && (
                  <p className="mt-1 text-xs text-neon-red">{errors.brandName}</p>
                )}
              </div>
            ) : (
              <div>
                <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                  Agency Name
                </label>
                <input
                  type="text"
                  placeholder="e.g. Creative Studio"
                  value={form.agencyName}
                  onChange={e => set('agencyName', e.target.value)}
                  className={inputBase}
                />
                {errors.agencyName && (
                  <p className="mt-1 text-xs text-neon-red">{errors.agencyName}</p>
                )}
              </div>
            )}

            {/* Mobile */}
            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Mobile Number
              </label>
              <input
                type="tel"
                placeholder="+91 9876543210"
                value={form.mobile}
                onChange={e => set('mobile', e.target.value)}
                className={inputBase}
              />
              {errors.mobile && (
                <p className="mt-1 text-xs text-neon-red">{errors.mobile}</p>
              )}
            </div>

            {/* Password */}
            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Min. 8 characters"
                  value={form.password}
                  onChange={e => set('password', e.target.value)}
                  className={inputBase + ' pr-10'}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(p => !p)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? (
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
              {errors.password && (
                <p className="mt-1 text-xs text-neon-red">{errors.password}</p>
              )}
            </div>

            {/* Invite Code */}
            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Invite Code
              </label>
              <input
                type="text"
                placeholder="Enter your invite code"
                value={form.inviteCode}
                onChange={e => set('inviteCode', e.target.value)}
                className={inputBase + ' font-mono tracking-widest uppercase'}
              />
              {errors.inviteCode && (
                <p className="mt-1 text-xs text-neon-red">{errors.inviteCode}</p>
              )}
            </div>

            {/* Security Questions */}
            <div className="pt-2 border-t border-surface-light-border dark:border-surface-dark-border">
              <p className="text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-3">
                Security Questions
              </p>
              <div className="space-y-4">
                {([1, 2] as const).map(n => {
                  const qKey = `securityQuestion${n}` as const
                  const aKey = `securityAnswer${n}` as const
                  const otherQ = n === 1 ? form.securityQuestion2 : form.securityQuestion1
                  return (
                    <div key={n} className="space-y-2">
                      <select
                        value={form[qKey]}
                        onChange={e => set(qKey, e.target.value)}
                        disabled={questions.length === 0}
                        className={inputBase + ' cursor-pointer disabled:opacity-50'}
                      >
                        {questions.length === 0 ? (
                          <option value="">Loading…</option>
                        ) : (
                          questions
                            .filter(q => q !== otherQ)
                            .map(q => <option key={q} value={q}>{q}</option>)
                        )}
                      </select>
                      <input
                        type="text"
                        placeholder={`Answer ${n}`}
                        value={form[aKey]}
                        onChange={e => set(aKey, e.target.value)}
                        className={inputBase}
                      />
                      {errors[aKey] && (
                        <p className="text-xs text-neon-red">{errors[aKey]}</p>
                      )}
                    </div>
                  )
                })}
              </div>
            </div>

            {apiError && (
              <p className="text-xs text-neon-red text-center">{apiError}</p>
            )}

            <Button type="submit" variant="primary" size="lg" loading={submitting} className="w-full mt-2">
              Create Account
            </Button>
          </form>
        </div>

        <p className="text-center text-xs text-ink-light-muted dark:text-ink-dark-muted mt-6">
          Already have an account?{' '}
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
    </>
  )
}
