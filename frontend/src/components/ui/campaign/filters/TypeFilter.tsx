import type { CampaignType } from '../../../../types'
import { CAMPAIGN_TYPE_LABELS } from '../../../../constants/campaigns'
import { TYPE_COLORS } from './chipColors'

const TYPES: CampaignType[] = ['CAMPAIGN_TYPE_RATING', 'CAMPAIGN_TYPE_REVIEW', 'CAMPAIGN_TYPE_ORDER', 'CAMPAIGN_TYPE_DISCOUNT']

const inactive = 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover'

interface Props {
  selected: Set<CampaignType>
  onChange: (selected: Set<CampaignType>) => void
}

export function TypeFilter({ selected, onChange }: Props) {
  function toggle(t: CampaignType) {
    const next = new Set(selected)
    if (next.has(t)) next.delete(t); else next.add(t)
    onChange(next)
  }

  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">Campaign Type</p>
      <div className="flex flex-wrap gap-1.5">
        {TYPES.map(t => {
          const colors = TYPE_COLORS[t]
          return (
            <button
              key={t}
              onClick={() => toggle(t)}
              className={['px-3 py-1 rounded-full text-xs font-medium border transition-all', selected.has(t) ? colors.base : inactive].join(' ')}
            >
              {CAMPAIGN_TYPE_LABELS[t]}
            </button>
          )
        })}
      </div>
    </div>
  )
}
