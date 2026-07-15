import { ScoreBadge } from '../../utils/ScoreBadge'
import { MatchStatusIcon } from './MatchStatusIcon'
import type { ExtractedField } from './ClaimProofGallery'

interface Props {
  fields: ExtractedField[]
}

function FieldValue({ value }: { value: string | undefined }) {
  return (
    <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary break-all">
      {value || <span className="text-ink-light-muted dark:text-ink-dark-muted font-normal">—</span>}
    </span>
  )
}

export function ClaimProofCompareTable({ fields }: Props) {
  if (fields.length === 0) {
    return <p className="p-3 text-xs text-ink-light-muted dark:text-ink-dark-muted">No extracted data.</p>
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-xs border-collapse table-fixed">
        <colgroup>
          <col className="w-[16%]" />
          <col className="w-[26%]" />
          <col className="w-[32%]" />
          <col className="w-[26%]" />
        </colgroup>
        <thead>
          <tr className="border-b-2 border-surface-light-border dark:border-surface-dark-border bg-surface-light-raised dark:bg-surface-dark-raised">
            <th className="text-left py-2 px-3 font-semibold text-ink-light-muted dark:text-ink-dark-muted">Field</th>
            <th className="text-left py-2 px-3 font-semibold text-neon-cyan/80">🏷 Campaign (Base)</th>
            <th className="text-left py-2 px-3 font-semibold text-neon-blue/80">🤖 Extracted (AI)</th>
            <th className="text-left py-2 px-3 font-semibold text-neon-purple/80">👤 Submitted (Manual)</th>
          </tr>
        </thead>
        <tbody>
          {fields.map(f => (
            <tr
              key={f.key ?? f.label}
              className={f.submittedMismatch ? 'bg-neon-red/[0.04]' : ''}
            >
              <td className="py-1.5 px-3 border-b border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary font-medium whitespace-nowrap">
                {f.label}
              </td>
              <td className="py-1.5 px-3 border-b border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary">
                {f.campaignValue ?? <span className="text-ink-light-muted dark:text-ink-dark-muted">—</span>}
              </td>
              <td className="py-1.5 px-3 border-b border-surface-light-border dark:border-surface-dark-border">
                <div className="flex flex-col gap-0.5">
                  <div className="flex items-center gap-1.5">
                    {f.score == null && <MatchStatusIcon matched={f.matched} indeterminate={f.indeterminate} />}
                    <ScoreBadge score={f.score} className="" />
                  </div>
                  <FieldValue value={f.value} />
                </div>
              </td>
              <td className={[
                'py-1.5 px-3 border-b border-surface-light-border dark:border-surface-dark-border break-all font-semibold',
                f.submittedMismatch
                  ? 'text-neon-red'
                  : 'text-ink-light-primary dark:text-ink-dark-primary',
              ].join(' ')}>
                {f.submittedValue
                  ? <>{f.submittedMismatch && <span className="text-[9px] mr-0.5 opacity-70">⚠</span>}{f.submittedValue}</>
                  : <span className="text-ink-light-muted dark:text-ink-dark-muted font-normal">—</span>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
