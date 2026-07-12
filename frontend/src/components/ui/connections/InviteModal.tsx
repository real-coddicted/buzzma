import { useState } from 'react'
import { Modal } from '../Modal'
import { Button } from '../Button'

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
    <Modal onClose={onClose}>
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

      <Button variant="secondary" size="md" className="w-full" onClick={onClose}>
        Close
      </Button>
    </Modal>
  )
}
