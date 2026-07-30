import { Modal } from './Modal'

interface AlertModalProps {
  title: string
  message: string
  dismissLabel?: string
  onDismiss: () => void
}

export function AlertModal({ title, message, dismissLabel = 'Got it', onDismiss }: AlertModalProps) {
  return (
    <Modal onClose={onDismiss}>
      <h2 className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">
        {title}
      </h2>
      <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary leading-snug">
        {message}
      </p>
      <div className="flex pt-1">
        <button
          onClick={onDismiss}
          className="flex-1 py-2 text-sm font-semibold rounded-lg border border-neon-blue/40 bg-neon-blue/10 text-neon-blue hover:bg-neon-blue/20 transition-all"
        >
          {dismissLabel}
        </button>
      </div>
    </Modal>
  )
}
