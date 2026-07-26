import type { PaymentBatch } from '../../../types/MyPaymentsTypes'

interface ProofModalProps {
  batch: PaymentBatch
  onClose: () => void
}

export function ProofModal({ batch, onClose }: ProofModalProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="relative bg-surface-light-card dark:bg-surface-dark-card rounded-2xl border border-surface-light-border dark:border-surface-dark-border shadow-xl max-w-lg w-full mx-4 overflow-hidden"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <div>
            <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">Payment Proof</p>
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{batch.date} · {batch.agencyName}</p>
          </div>
          <button
            onClick={onClose}
            className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover text-ink-light-muted dark:text-ink-dark-muted transition-colors"
          >
            ✕
          </button>
        </div>
        <div className="p-6 flex flex-col items-center justify-center gap-3 min-h-64 bg-surface-light-base dark:bg-surface-dark-base">
          {batch.proofStorageKey ? (
            <img src={batch.proofStorageKey} alt="Payment proof" className="max-w-full max-h-96 rounded-lg object-contain" />
          ) : (
            <>
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="text-ink-light-muted dark:text-ink-dark-muted">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <circle cx="8.5" cy="8.5" r="1.5" />
                <polyline points="21 15 16 10 5 21" />
              </svg>
              <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary">Screenshot proof of payment</p>
              <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">(Real image loads from API)</p>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
