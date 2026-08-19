import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { IconX } from './icons'
import { Button } from './Button'
import { sendEmailOtp, verifyEmailOtp } from '../../api/userApi'

interface VerifyEmailModalProps {
  email: string
  onClose: () => void
  onVerified: () => void
}

const RESEND_COOLDOWN_SECONDS = 60

const inputBase =
  'w-full rounded-lg border bg-surface-light-base dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm text-center tracking-[0.5em] outline-none transition-colors ' +
  'focus:border-neon-blue focus:ring-1 focus:ring-neon-blue/30'

export function VerifyEmailModal({ email, onClose, onVerified }: VerifyEmailModalProps) {
  const [code, setCode] = useState('')
  const [codeError, setCodeError] = useState('')
  const [sending, setSending] = useState(true)
  const [verifying, setVerifying] = useState(false)
  const [apiError, setApiError] = useState('')
  const [cooldown, setCooldown] = useState(0)
  const sentOnce = useRef(false)

  useEffect(() => {
    if (sentOnce.current) return
    sentOnce.current = true
    void doSend()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setInterval(() => setCooldown(c => Math.max(0, c - 1)), 1000)
    return () => clearInterval(timer)
  }, [cooldown])

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape' && !verifying) onClose()
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose, verifying])

  async function doSend() {
    setSending(true)
    setApiError('')
    try {
      await sendEmailOtp()
      setCooldown(RESEND_COOLDOWN_SECONDS)
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Failed to send verification code.')
    } finally {
      setSending(false)
    }
  }

  async function handleVerify(e: React.FormEvent) {
    e.preventDefault()
    e.stopPropagation()
    const trimmed = code.trim()
    if (!/^\d{6}$/.test(trimmed)) {
      setCodeError('Enter the 6-digit code from your email')
      return
    }
    setCodeError('')
    setVerifying(true)
    setApiError('')
    try {
      await verifyEmailOtp(trimmed)
      onVerified()
      onClose()
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Verification failed. Please try again.')
    } finally {
      setVerifying(false)
    }
  }

  return createPortal(
    <>
      <div
        className="fixed inset-0 bg-black/40 z-[200]"
        onClick={verifying ? undefined : onClose}
      />
      <div className="fixed right-0 top-0 bottom-0 w-full sm:w-96 z-[201] bg-surface-light-card dark:bg-surface-dark-card border-l border-surface-light-border dark:border-surface-dark-border flex flex-col shadow-2xl">
        <div className="flex items-center justify-between px-5 py-4 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
          <span className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">Verify your email</span>
          <button
            onClick={verifying ? undefined : onClose}
            disabled={verifying}
            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <IconX size={15} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-5 py-4">
          <form id="verify-email-form" onSubmit={handleVerify} className="space-y-2">
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              {sending
                ? `Sending a verification code to ${email}...`
                : `Enter the 6-digit code we sent to ${email}.`}
            </p>

            {!sending && (
              <p className="text-xs text-neon-yellow bg-neon-yellow/10 border border-neon-yellow/30 rounded-lg px-3 py-2">
                Don't see it? Check your spam folder.
              </p>
            )}

            <input
              type="text"
              inputMode="numeric"
              autoComplete="one-time-code"
              maxLength={6}
              placeholder="000000"
              value={code}
              disabled={sending}
              onChange={e => {
                setCode(e.target.value.replace(/\D/g, '').slice(0, 6))
                setCodeError('')
              }}
              className={inputBase}
            />
            {codeError && <p className="text-xs text-neon-red">{codeError}</p>}
            {apiError && <p className="text-xs text-neon-red">{apiError}</p>}

            <div className="pt-1">
              <button
                type="button"
                onClick={() => void doSend()}
                disabled={sending || cooldown > 0}
                className="text-xs font-medium text-neon-blue disabled:text-ink-light-muted dark:disabled:text-ink-dark-muted disabled:cursor-not-allowed hover:underline"
              >
                {cooldown > 0 ? `Resend code in ${cooldown}s` : 'Resend code'}
              </button>
            </div>
          </form>
        </div>

        <div className="flex items-center gap-2 px-5 py-4 border-t border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
          <Button type="button" variant="secondary" size="sm" className="flex-1" onClick={onClose} disabled={verifying}>
            Cancel
          </Button>
          <Button type="submit" form="verify-email-form" variant="primary" size="sm" className="flex-1" loading={verifying} disabled={sending}>
            Verify
          </Button>
        </div>
      </div>
    </>,
    document.body,
  )
}