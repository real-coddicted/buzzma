import { useEffect, useState } from 'react'
import { fetchScreenshotUrl } from '../../../api/claimApi'
import { Loading } from '../Loading'
import { Toast } from '../Toast'
import { ProofPlaceholder } from '../ProofThumbnail'
import type { MadePayment } from '../../../types/UserPayoutsTypes'

interface PayoutProofModalProps {
  payment: MadePayment
  onClose: () => void
}

export function PayoutProofModal({ payment, onClose }: PayoutProofModalProps) {
  const [url, setUrl] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!payment.screenshotStorageKey) return
    let objectUrl: string | null = null
    let cancelled = false
    setLoading(true)
    fetchScreenshotUrl(payment.screenshotStorageKey)
      .then(u => { if (cancelled) return; objectUrl = u; setUrl(u) })
      .catch(() => { if (!cancelled) setError('Failed to load payment proof. Please try again.') })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [payment.screenshotStorageKey])

  useEffect(() => {
    function onKey(e: KeyboardEvent) { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose])

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
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{payment.paidAt} · {payment.paymentMethod}</p>
          </div>
          <button
            onClick={onClose}
            className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover text-ink-light-muted dark:text-ink-dark-muted transition-colors"
          >
            ✕
          </button>
        </div>
        <div className="p-6 flex flex-col items-center justify-center gap-3 min-h-64 bg-surface-light-base dark:bg-surface-dark-base">
          {loading ? (
            <Loading size={32} />
          ) : url ? (
            <img src={url} alt="Payment proof" className="max-w-full max-h-96 rounded-lg object-contain" />
          ) : (
            <ProofPlaceholder />
          )}
        </div>
      </div>
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}
