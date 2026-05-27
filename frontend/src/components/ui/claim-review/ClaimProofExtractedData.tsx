import { IconCheck, IconX } from '../icons'
import type { ExtractedField } from './ClaimProofGallery'

interface ClaimProofExtractedDataProps {
  fields: ExtractedField[]
}

export function ClaimProofExtractedData({ fields }: ClaimProofExtractedDataProps) {
  return (
    <ul className="flex flex-col divide-y divide-surface-light-border dark:divide-surface-dark-border">
      {fields.map(field => (
        <li
          key={field.label}
          className="flex items-center justify-between gap-4 py-2 first:pt-0 last:pb-0"
        >
          <div className="flex items-center gap-2.5 min-w-0">
            {field.matched ? (
              <span
                className="flex-shrink-0 inline-flex items-center justify-center w-5 h-5 rounded-full bg-neon-green/15 text-neon-green"
                title="Matched"
              >
                <IconCheck size={12} />
              </span>
            ) : (
              <span
                className="flex-shrink-0 inline-flex items-center justify-center w-5 h-5 rounded-full bg-neon-red/15 text-neon-red"
                title="Mismatched"
              >
                <IconX size={12} />
              </span>
            )}
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              {field.label}
            </span>
          </div>
          <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary text-right truncate min-w-0">
            {field.value}
          </span>
        </li>
      ))}
    </ul>
  )
}
