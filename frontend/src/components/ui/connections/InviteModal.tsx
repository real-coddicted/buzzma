import { useState } from 'react'

interface InviteModalProps {
  code: string | null
  loading: boolean
  onClose: () => void
}

export function InviteModal({ code, loading, onClose }: InviteModalProps) {
  const [copied, setCopied] = useState(false)

  function handleCopy() {
    navigator.clipboard.writeText(code ?? '').then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      onClick={onClose}
    >
      <div
        className="w-full max-w-sm rounded-2xl bg-surface-light-card dark:bg-surface-dark-card border border-surface-light-border dark:border-surface-dark-border p-6 shadow-xl space-y-4"
        onClick={e => e.stopPropagation()}
      >
        <h2 className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          Your Invite Code
        </h2>

        {loading ? (
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">Fetching code…</p>
        ) : (
          <div className="flex items-center gap-3 rounded-lg bg-surface-light-raised dark:bg-surface-dark-raised border border-surface-light-border dark:border-surface-dark-border px-4 py-3">
            <span className="flex-1 font-mono text-sm tracking-widest text-neon-blue">
              {code}
            </span>
            <button
              onClick={handleCopy}
              className={['text-xs font-semibold transition-colors', copied ? 'text-neon-green' : 'text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-blue'].join(' ')}
            >
              {copied ? 'Copied!' : 'Copy'}
            </button>
          </div>
        )}

        <button
          onClick={onClose}
          className="w-full py-2 text-sm font-semibold rounded-lg bg-surface-light-raised dark:bg-surface-dark-raised text-ink-light-primary dark:text-ink-dark-primary hover:brightness-110 transition-all"
        >
          Close
        </button>
      </div>
    </div>
  )
}
