import { useState } from 'react'
import { Modal } from '../Modal'
import { Button } from '../Button'

interface RequestConnectionModalProps {
  submitting: boolean
  error: string | null
  onSubmit: (code: string) => void
  onClose: () => void
}

export function RequestConnectionModal({ submitting, error, onSubmit, onClose }: RequestConnectionModalProps) {
  const [code, setCode] = useState('')

  function handleSubmit() {
    if (!code.trim() || submitting) return
    onSubmit(code.trim())
  }

  return (
    <Modal onClose={submitting ? undefined : onClose}>
      <h2 className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">
        Request to connect
      </h2>
      <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary leading-snug">
        Enter the invite code your contact shared with you.
      </p>

      <input
        type="text"
        value={code}
        onChange={e => setCode(e.target.value.toUpperCase())}
        placeholder="e.g. 7F3-K9Q"
        disabled={submitting}
        className="w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover border-surface-light-border dark:border-surface-dark-border text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted font-mono tracking-widest uppercase px-4 py-3 text-sm outline-none focus:border-neon-yellow/60"
      />
      {error && <p className="text-xs text-neon-red">{error}</p>}

      <div className="flex gap-2 pt-1">
        <Button variant="secondary" className="flex-1" onClick={onClose} disabled={submitting}>
          Cancel
        </Button>
        <Button variant="yellow" className="flex-1" onClick={handleSubmit} disabled={submitting || !code.trim()}>
          {submitting ? 'Sending…' : 'Send Request'}
        </Button>
      </div>
    </Modal>
  )
}