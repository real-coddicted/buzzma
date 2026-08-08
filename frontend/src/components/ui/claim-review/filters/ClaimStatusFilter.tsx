import type { ClaimStatus } from '../../../../types'
import { CLAIM_STATUS_CONFIG } from '../claimReviewConstants'
import { CLAIM_STATUS_COLORS } from '../../campaign/filters/chipColors'

const inactive = 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover'

interface Props {
  selected: Set<ClaimStatus>
  onChange: (selected: Set<ClaimStatus>) => void
}

export function ClaimStatusFilter({ selected, onChange }: Props) {
  function toggle(s: ClaimStatus) {
    const next = new Set(selected)
    if (next.has(s)) next.delete(s); else next.add(s)
    onChange(next)
  }

  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">Claim Status</p>
      <div className="flex flex-wrap gap-1.5">
        {(Object.keys(CLAIM_STATUS_CONFIG) as ClaimStatus[]).map(value => {
          const { label } = CLAIM_STATUS_CONFIG[value]
          const colors = CLAIM_STATUS_COLORS[value]
          return (
            <button
              key={value}
              onClick={() => toggle(value)}
              className={['px-3 py-1 rounded-full text-xs font-medium border transition-all', selected.has(value) ? colors.base : inactive].join(' ')}
            >
              {label}
            </button>
          )
        })}
      </div>
    </div>
  )
}