import type { CampaignStatus } from '../../../../types'
import { CAMPAIGN_STATUS_CONFIG } from '../../../../types'
import { STATUS_COLORS } from './chipColors'

const inactive = 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover'

interface Props {
  selected: Set<CampaignStatus>
  onChange: (selected: Set<CampaignStatus>) => void
}

export function StatusFilter({ selected, onChange }: Props) {
  function toggle(s: CampaignStatus) {
    const next = new Set(selected)
    if (next.has(s)) next.delete(s); else next.add(s)
    onChange(next)
  }

  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">Status</p>
      <div className="flex flex-wrap gap-1.5">
        {(Object.keys(CAMPAIGN_STATUS_CONFIG) as CampaignStatus[]).map(value => {
          const { label } = CAMPAIGN_STATUS_CONFIG[value]
          const colors = STATUS_COLORS[value]
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
