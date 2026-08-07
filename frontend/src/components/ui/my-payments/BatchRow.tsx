import { useEffect, useState } from 'react'
import { Badge } from '../Badge'
import { IconChevronRight } from '../icons'
import { formatRupees } from '../../../utils/currency'
import { fetchScreenshotUrl } from '../../../api/claimApi'
import type { PaymentBatch } from '../../../types/MyPaymentsTypes'

interface BatchRowProps {
  batch: PaymentBatch
  isLast: boolean
  onClick: () => void
  onProofClick: (batch: PaymentBatch) => void
}

function ProofPlaceholderIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="text-ink-light-muted dark:text-ink-dark-muted">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="8.5" cy="8.5" r="1.5" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  )
}

function ProofThumbnail({ storageKey }: { storageKey?: string }) {
  const [url, setUrl] = useState<string | null>(null)

  useEffect(() => {
    setUrl(null)
    if (!storageKey) return
    let objectUrl: string | null = null
    let cancelled = false
    fetchScreenshotUrl(storageKey)
      .then(u => { if (cancelled) return; objectUrl = u; setUrl(u) })
      .catch(() => {})
    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [storageKey])

  return url ? (
    <img src={url} alt="Payment proof" className="w-full h-full object-cover rounded-xl" />
  ) : (
    <ProofPlaceholderIcon />
  )
}

export function BatchRow({ batch, isLast, onClick, onProofClick }: BatchRowProps) {
  return (
    <li
      className={[
        'flex items-center gap-4 px-4 py-3 cursor-pointer hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors',
        !isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : '',
      ].join(' ')}
      onClick={onClick}
    >
      <button
        className="shrink-0 w-12 h-12 rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover flex items-center justify-center overflow-hidden hover:border-neon-blue/40 transition-colors"
        title="View payment proof"
        onClick={e => { e.stopPropagation(); onProofClick(batch) }}
      >
        <ProofThumbnail storageKey={batch.proofStorageKey} />
      </button>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">{batch.date}</p>
        <div className="flex items-center gap-2 mt-0.5 flex-wrap">
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{batch.agencyName}</span>
          <span className="text-ink-light-muted dark:text-ink-dark-muted text-xs">·</span>
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{batch.claimCount} claims paid</span>
          <span className="text-ink-light-muted dark:text-ink-dark-muted text-xs">·</span>
          <Badge variant="purple">{batch.paymentMode}</Badge>
        </div>
      </div>
      <p className="text-sm font-bold text-neon-green shrink-0">₹{formatRupees(batch.totalAmount)}</p>
      <IconChevronRight className="text-ink-light-muted dark:text-ink-dark-muted shrink-0" />
    </li>
  )
}
