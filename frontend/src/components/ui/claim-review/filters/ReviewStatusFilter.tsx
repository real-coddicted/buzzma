import type { ReviewStatus } from '../../../../types'
import { REVIEW_STATUS_CONFIG } from '../claimReviewConstants'

const inactive = 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover'

interface Props {
  selected: Set<ReviewStatus>
  onChange: (selected: Set<ReviewStatus>) => void
}

export function ReviewStatusFilter({ selected, onChange }: Props) {
  function toggle(s: ReviewStatus) {
    const next = new Set(selected)
    if (next.has(s)) next.delete(s); else next.add(s)
    onChange(next)
  }

  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">Review Status</p>
      <div className="flex flex-wrap gap-1.5">
        {(Object.keys(REVIEW_STATUS_CONFIG) as ReviewStatus[]).map(value => {
          const { label, activeClass } = REVIEW_STATUS_CONFIG[value]
          return (
            <button
              key={value}
              onClick={() => toggle(value)}
              className={['px-3 py-1 rounded-full text-xs font-medium border transition-all', selected.has(value) ? activeClass : inactive].join(' ')}
            >
              {label}
            </button>
          )
        })}
      </div>
    </div>
  )
}
