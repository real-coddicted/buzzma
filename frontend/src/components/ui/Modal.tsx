import type { ReactNode } from 'react'

interface ModalProps {
  /** Backdrop click handler. Pass `undefined` while busy to block dismissal. */
  onClose?: () => void
  children: ReactNode
}

export function Modal({ onClose, children }: ModalProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 animate-fade-in"
      onClick={onClose}
    >
      <div
        className="w-full max-w-sm rounded-2xl bg-surface-light-card dark:bg-surface-dark-card border border-surface-light-border dark:border-surface-dark-border p-6 shadow-xl space-y-4"
        onClick={e => e.stopPropagation()}
      >
        {children}
      </div>
    </div>
  )
}