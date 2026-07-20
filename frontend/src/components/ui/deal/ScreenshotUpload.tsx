import { useEffect, useRef, useState } from 'react'
import { extractOrderDetails, type ExtractionResponse } from '../../../api/extractionApi'
import { Toast } from '../Toast'
import { ImageLightbox } from './ImageLightbox'

interface ScreenshotUploadProps {
  label: string
  hint?: string
  campaignId?: string
  onExtract?: (data: ExtractionResponse) => void
  onFileChange?: (file: File) => void
  initialPreview?: string
}

function resolveError(raw: string): string {
  if (raw.includes('timed out') || raw.includes('AbortError')) return raw
  if (raw.includes('Session expired')) return raw
  return 'Screenshot data extraction failed. Please try again or contact support if the issue persists.'
}

export function ScreenshotUpload({ label, hint, campaignId, onExtract, onFileChange, initialPreview }: ScreenshotUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [fileName, setFileName] = useState<string | null>(null)
  const [preview, setPreview] = useState<string | null>(initialPreview ?? null)
  const [lightboxOpen, setLightboxOpen] = useState(false)

  useEffect(() => {
    if (initialPreview) setPreview(prev => prev ?? initialPreview)
  }, [initialPreview])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleFile(file: File | undefined) {
    if (!file) return

    setFileName(file.name)
    setPreview(URL.createObjectURL(file))
    setError(null)
    onFileChange?.(file)

    if (onExtract && campaignId) {
      setIsLoading(true)
      try {
        const extractedData = await extractOrderDetails(file, campaignId)
        onExtract(extractedData)
      } catch (err) {
        const raw = err instanceof Error ? err.message : ''
        setError(resolveError(raw))
      } finally {
        setIsLoading(false)
      }
    }
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault()
    handleFile(e.dataTransfer.files[0])
  }

  return (
    <div className="space-y-1.5">
      <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary">
        {label} <span className="text-neon-red">*</span>
      </label>
      {hint && (
        <p className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">{hint}</p>
      )}
      <div
        onClick={() => !isLoading && inputRef.current?.click()}
        onDrop={e => !isLoading && handleDrop(e)}
        onDragOver={e => e.preventDefault()}
        className={`w-full rounded-xl border-2 border-dashed border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/40 transition-colors ${!isLoading ? 'cursor-pointer' : 'cursor-not-allowed opacity-60'} overflow-hidden`}
      >
        {preview ? (
          <div className="relative">
            <img src={preview} alt="preview" className="w-full max-h-48 object-contain bg-surface-light-hover dark:bg-surface-dark-hover" />
            {!isLoading && (
              <button
                type="button"
                onClick={e => { e.stopPropagation(); setLightboxOpen(true) }}
                className="absolute top-2 right-2 text-xs px-2 py-1 rounded-lg bg-black/50 text-white hover:bg-black/70 transition-colors"
              >
                ⤢ View full
              </button>
            )}
            {isLoading && (
              <div className="absolute inset-0 flex items-center justify-center bg-black/30 backdrop-blur-sm">
                <div className="flex flex-col items-center gap-2">
                  <div className="w-6 h-6 border-2 border-neon-blue border-t-transparent rounded-full animate-spin" />
                  <span className="text-xs text-white font-medium">Extracting details...</span>
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="flex flex-col items-center gap-1.5 px-4 py-8 text-center">
            <span className="text-2xl">🖼</span>
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              Drop screenshot here or click to upload
            </span>
          </div>
        )}
      </div>
      {fileName && (
        <p className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted truncate">{fileName}</p>
      )}
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        disabled={isLoading}
        onChange={e => handleFile(e.target.files?.[0])}
      />
      {lightboxOpen && preview && (
        <ImageLightbox src={preview} alt={label} onClose={() => setLightboxOpen(false)} />
      )}
    </div>
  )
}
