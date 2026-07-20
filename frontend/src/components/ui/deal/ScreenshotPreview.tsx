import { useEffect, useState } from 'react'
import { fetchScreenshotUrl } from '../../../api/claimApi'
import { ImageLightbox } from './ImageLightbox'

interface ScreenshotPreviewProps {
  storageKey?: string
  label: string
}

export function ScreenshotPreview({ storageKey, label }: ScreenshotPreviewProps) {
  const [url, setUrl] = useState<string | null>(null)
  const [lightboxOpen, setLightboxOpen] = useState(false)

  useEffect(() => {
    if (!storageKey) return
    let objectUrl: string | null = null
    fetchScreenshotUrl(storageKey)
      .then(u => { objectUrl = u; setUrl(u) })
      .catch(() => {})
    return () => { if (objectUrl) URL.revokeObjectURL(objectUrl) }
  }, [storageKey])

  if (!url) return null

  return (
    <div>
      <p className="text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
        {label}
      </p>
      <div className="relative group cursor-pointer" onClick={() => setLightboxOpen(true)}>
        <img
          src={url}
          alt={label}
          className="w-full max-h-48 object-contain rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover"
        />
        <div className="absolute inset-0 flex items-center justify-center rounded-lg bg-black/0 group-hover:bg-black/30 transition-colors">
          <span className="opacity-0 group-hover:opacity-100 transition-opacity text-white text-xs font-semibold bg-black/50 px-2.5 py-1 rounded-lg">
            ⤢ View full
          </span>
        </div>
      </div>
      {lightboxOpen && <ImageLightbox src={url} alt={label} onClose={() => setLightboxOpen(false)} />}
    </div>
  )
}
