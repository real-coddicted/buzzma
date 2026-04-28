import { useState } from 'react'
import { Button } from '../components/ui/Button'
import { fetchUserSecurityQuestion } from '../api/authApi'
import type { LoginAs, ForgotPasswordForm } from '../types/ForgotPasswordTypes'

interface ForgotPasswordProps {
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

const mobileRegex = /^\+?[0-9]{7,15}$/

export function ForgotPassword({ onSuccess, onGoToLogin }: ForgotPasswordProps) {
  const [form, setForm] = useState<ForgotPasswordForm>({
    role: 'brand',
    mobile: '',
    securityQuestion: '',
    answer: '',
  })
  const [questionLoading, setQuestionLoading] = useState(false)
  const [errors, setErrors] = useState<Partial<Record<keyof ForgotPasswordForm, string>>>({})

  function set<K extends keyof ForgotPasswordForm>(key: K, value: ForgotPasswordForm[K]) {
    setForm(prev => ({ ...prev, [key]: value }))
    setErrors(prev => ({ ...prev, [key]: undefined }))
  }

  function handleMobileBlur() {
    const mobile = form.mobile.replace(/\s/g, '')
    if (!mobile || !mobileRegex.test(mobile)) return
    setQuestionLoading(true)
    setForm(prev => ({ ...prev, securityQuestion: '', answer: '' }))
    fetchUserSecurityQuestion(mobile)
      .then(q => setForm(prev => ({ ...prev, securityQuestion: q })))
      .finally(() => setQuestionLoading(false))
  }

  function validate(): boolean {
    const next: Partial<Record<keyof ForgotPasswordForm, string>> = {}
    if (!form.mobile.trim()) next.mobile = 'Mobile number is required'
    else if (!mobileRegex.test(form.mobile.replace(/\s/g, '')))
      next.mobile = 'Enter a valid mobile number'
    if (!form.answer.trim()) next.answer = 'Answer is required'
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
            Verify your identity
          </p>
        </div>

        <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-6">
          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            {/* Role */}
            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Role
              </label>
              <select
                value={form.role}
                onChange={e => set('role', e.target.value as LoginAs)}
                className={inputBase + ' cursor-pointer'}
              >
                <option value="brand">Brand</option>
                <option value="agency">Agency</option>
              </select>
            </div>

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
                onBlur={handleMobileBlur}
                className={inputBase}
              />
              {errors.mobile && (
                <p className="mt-1 text-xs text-neon-red">{errors.mobile}</p>
              )}
            </div>

            {/* Security Question — shown after mobile lookup */}
            {(questionLoading || form.securityQuestion) && (
              <div>
                <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                  Security Question
                </label>

                {questionLoading ? (
                  <div className="flex items-center gap-2 px-3 py-2.5 rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
                    <span className="inline-block w-3.5 h-3.5 border-2 border-neon-blue border-t-transparent rounded-full animate-spin flex-shrink-0" />
                    <span className="text-sm text-ink-light-muted dark:text-ink-dark-muted">Fetching your question…</span>
                  </div>
                ) : (
                  <div className="px-3 py-2.5 rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-sm text-ink-light-primary dark:text-ink-dark-primary">
                    {form.securityQuestion}
                  </div>
                )}
              </div>
            )}

            {/* Answer — only once question is loaded */}
            {form.securityQuestion && !questionLoading && (
              <div>
                <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                  Answer
                </label>
                <input
                  type="text"
                  placeholder="Your answer"
                  value={form.answer}
                  onChange={e => set('answer', e.target.value)}
                  className={inputBase}
                />
                {errors.answer && (
                  <p className="mt-1 text-xs text-neon-red">{errors.answer}</p>
                )}
              </div>
            )}

            <Button type="submit" variant="primary" size="lg" className="w-full mt-2">
              Submit
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
