import { useRef, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import { PROOF_UPLOAD_LABEL } from '../../../constants/claim'
import { IconCheck } from '../icons'

interface ClaimStepProofUploadProps {
  deal: Deal
}

export function ClaimStepProofUpload({ deal }: ClaimStepProofUploadProps) {
  const uploadLabel = PROOF_UPLOAD_LABEL[deal.dealType]

  if (uploadLabel === null) {
    return (
      <div className="flex flex-col items-center justify-center gap-3 py-10 text-center">
        <div className="w-12 h-12 rounded-full bg-neon-green/10 border border-neon-green/30 flex items-center justify-center">
          <IconCheck size={22} className="text-neon-green" />
        </div>
        <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          No Action Required
        </p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted max-w-xs leading-relaxed">
          Your order type is automatically verified. No additional proof upload is needed.
        </p>
      </div>
    )
  }

  return <ProofUploadForm label={uploadLabel} />
}

function ProofUploadForm({ label }: { label: string }) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [fileName, setFileName] = useState<string | null>(null)

  function handleFile(file: File | undefined) {
    if (file) setFileName(file.name)
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault()
    handleFile(e.dataTransfer.files[0])
  }

  return (
    <div className="space-y-4">
      <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        {label}
      </p>

      <div
        onClick={() => inputRef.current?.click()}
        onDrop={handleDrop}
        onDragOver={e => e.preventDefault()}
        className="w-full rounded-lg border-2 border-dashed border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/40 transition-colors px-4 py-8 flex flex-col items-center gap-2 cursor-pointer"
      >
        <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
          {fileName ?? 'Drop image here or click to upload'}
        </span>
      </div>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={e => handleFile(e.target.files?.[0])}
      />

      <button
        disabled={!fileName}
        className="w-full py-2.5 rounded-lg bg-neon-blue text-surface-dark-base text-sm font-semibold hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
        onClick={() => console.log('proof submitted', fileName)}
      >
        Submit Proof
      </button>
    </div>
  )
}
