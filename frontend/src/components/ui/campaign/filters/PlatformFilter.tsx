import type { Platform } from '../../../../types'
import { PLATFORM_LABELS } from '../../../../constants/campaigns'
import { PLATFORM_COLORS } from './chipColors'

const PLATFORMS: Platform[] = ['PLATFORM_AMAZON', 'PLATFORM_FLIPKART', 'PLATFORM_NYKAA', 'PLATFORM_MYNTRA']

const inactive = 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover'

interface Props {
  selected: Set<Platform>
  onChange: (selected: Set<Platform>) => void
}

export function PlatformFilter({ selected, onChange }: Props) {
  function toggle(p: Platform) {
    const next = new Set(selected)
    if (next.has(p)) next.delete(p); else next.add(p)
    onChange(next)
  }

  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">Platform</p>
      <div className="flex flex-wrap gap-1.5">
        {PLATFORMS.map(p => {
          const colors = PLATFORM_COLORS[p]
          return (
            <button
              key={p}
              onClick={() => toggle(p)}
              className={['px-3 py-1 rounded-full text-xs font-medium border transition-all', selected.has(p) ? colors.base : inactive].join(' ')}
            >
              {PLATFORM_LABELS[p]}
            </button>
          )
        })}
      </div>
    </div>
  )
}
