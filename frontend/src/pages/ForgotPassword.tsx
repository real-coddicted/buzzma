import { useState } from 'react'
import { APP_NAME } from '../constants/app'
import { Button } from '../components/ui/Button'
import { AuthBackground } from '../components/ui/AuthBackground'
import { fetchUserSecurityQuestion } from '../api/authApi'
import { isValidMobile } from '../utils/mobileValidation'
import type { ForgotPasswordForm } from '../types/ForgotPasswordTypes'

interface ForgotPasswordProps {
  initialForm?: ForgotPasswordForm
  onSuccess: (form: ForgotPasswordForm) => void
  onGoToLogin: () => void
}

const inputBase =
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm outline-none transition-colors ' +
  'focus:border-neon-purple focus:ring-1 focus:ring-neon-purple/30'

export function ForgotPassword({ initialForm, onSuccess, onGoToLogin }: ForgotPasswordProps) {
  const [form, setForm] = useState<ForgotPasswordForm>(initialForm ?? {
    mobile: '',
    questionId: '',
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
    if (!mobile || !isValidMobile(mobile)) return
    setQuestionLoading(true)
    setForm(prev => ({ ...prev, questionId: '', securityQuestion: '', answer: '' }))
    fetchUserSecurityQuestion(mobile)
      .then(result => {
        if (result.success) {
          setForm(prev => ({ ...prev, questionId: result.questionId, securityQuestion: result.question }))
        } else {
          setErrors(prev => ({ ...prev, mobile: result.message }))
        }
      })
      .catch(() => setErrors(prev => ({ ...prev, mobile: 'Something went wrong. Please try again.' })))
      .finally(() => setQuestionLoading(false))
  }

  function validate(): boolean {
    const next: Partial<Record<keyof ForgotPasswordForm, string>> = {}
    if (!form.mobile.trim()) next.mobile = 'Mobile number is required'
    else if (!isValidMobile(form.mobile))
      next.mobile = 'Enter a valid mobile number'
    else if (!form.questionId)
      next.mobile = errors.mobile ?? 'Enter your mobile number to load your security question'
    else if (!form.answer.trim()) next.answer = 'Answer is required'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (validate() && !questionLoading) {
      onSuccess({
        ...form,
        mobile: form.mobile.replace(/\s/g, ''),
        answer: form.answer.trim(),
      })
    }
  }

  return (
    <div className="min-h-screen bg-surface-light-base dark:bg-surface-dark-base flex items-center justify-center p-4 relative overflow-hidden">
      <AuthBackground variant="purple" />

      <div className="w-full max-w-md relative z-10">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2.5 mb-3">
            <div className="w-10 h-10 rounded-xl bg-neon-purple/15 border border-neon-purple/30 flex items-center justify-center shadow-neon-purple">
              <svg className="w-5 h-5 text-neon-purple" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <span className="text-3xl font-bold text-neon-purple tracking-tight">{APP_NAME}</span>
          </div>
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">Verify your identity</p>
        </div>

        <div className="rounded-2xl border border-neon-purple/20 bg-surface-light-card dark:bg-surface-dark-card shadow-neon-purple p-6">
          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            {/* Mobile */}
            <div>
              <label className="block text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Mobile Number
              </label>
              <input
                type="tel"
                placeholder="9876543210"
                maxLength={10}
                value={form.mobile}
                onChange={e => {
                  // a question loaded for the previous number no longer applies
                  setForm(prev => ({ ...prev, questionId: '', securityQuestion: '', answer: '' }))
                  set('mobile', e.target.value)
                }}
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
                    <span className="inline-block w-3.5 h-3.5 border-2 border-neon-purple border-t-transparent rounded-full animate-spin flex-shrink-0" />
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

            <Button type="submit" variant="purple" size="lg" className="w-full mt-2">
              Submit
            </Button>
          </form>
        </div>

        <p className="text-center text-xs text-ink-light-muted dark:text-ink-dark-muted mt-6">
          Remember your password?{' '}
          <button
            type="button"
            onClick={onGoToLogin}
            className="text-neon-purple hover:text-neon-cyan transition-colors font-medium"
          >
            Sign in
          </button>
        </p>
      </div>
    </div>
  )
}
