type ConfirmTone = 'red' | 'blue'

interface ConfirmModalProps {
  title: string
  message: string
  confirmLabel: string
  cancelLabel?: string
  /** Accent of the confirm button — `red` for destructive actions (default). */
  tone?: ConfirmTone
  /** While true the action is in flight: buttons disable and dismissal is blocked. */
  busy?: boolean
  onConfirm: () => void
  onCancel: () => void
}

const confirmToneClasses: Record<ConfirmTone, string> = {
  red:  'bg-neon-red/15  text-neon-red  border border-neon-red/40  hover:bg-neon-red/25',
  blue: 'bg-neon-blue/15 text-neon-blue border border-neon-blue/40 hover:bg-neon-blue/25',
}

export function ConfirmModal({
  title,
  message,
  confirmLabel,
  cancelLabel = 'Cancel',
  tone = 'red',
  busy = false,
  onConfirm,
  onCancel,
}: ConfirmModalProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 animate-fade-in"
      onClick={busy ? undefined : onCancel}
    >
      <div
        className="w-full max-w-sm rounded-2xl bg-surface-light-card dark:bg-surface-dark-card border border-surface-light-border dark:border-surface-dark-border p-6 shadow-xl space-y-4"
        onClick={e => e.stopPropagation()}
      >
        <h2 className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          {title}
        </h2>
        <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary leading-snug">
          {message}
        </p>

        <div className="flex gap-2 pt-1">
          <button
            onClick={onCancel}
            disabled={busy}
            className="flex-1 py-2 text-sm font-semibold rounded-lg bg-surface-light-raised dark:bg-surface-dark-raised text-ink-light-primary dark:text-ink-dark-primary hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {cancelLabel}
          </button>
          <button
            onClick={onConfirm}
            disabled={busy}
            className={[
              'flex-1 py-2 text-sm font-semibold rounded-lg transition-all disabled:opacity-40 disabled:cursor-not-allowed',
              confirmToneClasses[tone],
            ].join(' ')}
          >
            {busy ? 'Working…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}