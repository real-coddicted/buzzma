import { useEffect, useState } from 'react'
import { fetchScreenshotUrl } from '../../api/claimApi'

export function ProofPlaceholderIcon({ size = 20 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="text-ink-light-muted dark:text-ink-dark-muted">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="8.5" cy="8.5" r="1.5" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  )
}

export function ProofThumbnail({ storageKey }: { storageKey?: string }) {
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

export function ProofPlaceholder() {
  return (
    <>
      <ProofPlaceholderIcon size={48} />
      <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary">Screenshot proof of payment</p>
    </>
  )
}
