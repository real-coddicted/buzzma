import { useState, useCallback } from 'react'

interface Props {
  src: string
  alt: string
}

export function ProductThumbnail({ src, alt }: Props) {
  const [failed, setFailed] = useState(false)
  const onError = useCallback(() => setFailed(true), [])
  if (failed) {
    return (
      <div className="w-10 h-10 rounded-lg flex items-center justify-center bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border">
        <span className="text-[10px] font-bold text-ink-light-muted dark:text-ink-dark-muted select-none">
          {alt.charAt(0).toUpperCase()}
        </span>
      </div>
    )
  }
  return (
    <div className="w-10 h-10 rounded-lg overflow-hidden flex-shrink-0 bg-surface-light-hover dark:bg-surface-dark-hover">
      <img src={src} alt={alt} onError={onError}
        className="w-full h-full object-cover" />
    </div>
  )
}
