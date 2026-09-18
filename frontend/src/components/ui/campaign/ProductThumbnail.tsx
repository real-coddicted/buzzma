import { useState } from 'react'

interface Props {
  src: string
  alt: string
}

export function ProductThumbnail({ src, alt }: Props) {
  // Track the src that failed rather than a boolean, so a newly typed URL is retried.
  const [failedSrc, setFailedSrc] = useState<string | null>(null)

  if (!src || failedSrc === src) {
    return (
      <div className="w-10 h-10 rounded-lg flex items-center justify-center bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border">
        <span className="text-[10px] font-bold text-ink-light-muted dark:text-ink-dark-muted select-none">
          {(alt.charAt(0) || '?').toUpperCase()}
        </span>
      </div>
    )
  }
  return (
    <div className="w-10 h-10 rounded-lg overflow-hidden flex-shrink-0 bg-surface-light-hover dark:bg-surface-dark-hover">
      <img src={src} alt={alt} onError={() => setFailedSrc(src)}
        className="w-full h-full object-cover" />
    </div>
  )
}
