import { useRef, useState } from 'react'
import { extractOrderDetails, type ExtractionResponse } from '../../../api/extractionApi'

interface ScreenshotUploadProps {
  label: string
  hint?: string
  onExtract?: (data: ExtractionResponse) => void
}

export function ScreenshotUpload({ label, hint, onExtract }: ScreenshotUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [fileName, setFileName] = useState<string | null>(null)
  const [preview, setPreview] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleFile(file: File | undefined) {
    if (!file) return
    
    setFileName(file.name)
    setPreview(URL.createObjectURL(file))
    setError(null)
    
    if (onExtract) {
      setIsLoading(true)
      try {
        const extractedData = await extractOrderDetails(file)
        onExtract(extractedData)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Failed to extract order details'
        setError(message)
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
      {error && (
        <p className="text-[11px] text-neon-red">{error}</p>
      )}
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        disabled={isLoading}
        onChange={e => handleFile(e.target.files?.[0])}
      />
    </div>
  )
}
