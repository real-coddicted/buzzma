import { useRef, useState } from 'react'

interface ScreenshotUploadProps {
  label: string
  hint?: string
}

export function ScreenshotUpload({ label, hint }: ScreenshotUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [fileName, setFileName] = useState<string | null>(null)
  const [preview, setPreview]   = useState<string | null>(null)

  function handleFile(file: File | undefined) {
    if (!file) return
    setFileName(file.name)
    setPreview(URL.createObjectURL(file))
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
        onClick={() => inputRef.current?.click()}
        onDrop={handleDrop}
        onDragOver={e => e.preventDefault()}
        className="w-full rounded-xl border-2 border-dashed border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/40 transition-colors cursor-pointer overflow-hidden"
      >
        {preview ? (
          <img src={preview} alt="preview" className="w-full max-h-48 object-contain bg-surface-light-hover dark:bg-surface-dark-hover" />
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
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={e => handleFile(e.target.files?.[0])}
      />
    </div>
  )
}
