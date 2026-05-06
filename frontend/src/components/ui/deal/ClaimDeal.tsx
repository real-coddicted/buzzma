import { useRef, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import { DealOrderForm } from './DealOrderForm'

interface ClaimDealProps {
  deal: Deal
}

export function ClaimDeal({ deal }: ClaimDealProps) {
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
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 space-y-5">
      <h3 className="text-base font-bold text-ink-light-primary dark:text-ink-dark-primary">
        Claim Deal
      </h3>

      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        Purchase this product on {deal.platformLabel} at the offered price and submit your order details to claim the deal.
      </p>

      <div>
        <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
          Screenshot <span className="text-neon-red">*</span>
        </label>
        <div
          onClick={() => inputRef.current?.click()}
          onDrop={handleDrop}
          onDragOver={e => e.preventDefault()}
          className="w-full rounded-lg border-2 border-dashed border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/40 transition-colors px-4 py-6 flex flex-col items-center gap-2 cursor-pointer"
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
      </div>

      <DealOrderForm onSubmit={fields => console.log('claim submitted', fields)} />
    </div>
  )
}
