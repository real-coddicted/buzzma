import type { DealStatus } from '../../../types/DealTypes'

export type DealTab = DealStatus

interface Tab {
  value: DealTab
  label: string
}

const tabs: Tab[] = [
  { value: 'explore',     label: 'Explore'  },
  { value: 'in_progress', label: 'Claimed'  },
]

interface DealTabsProps {
  value: DealTab
  counts: Record<DealTab, number>
  onChange: (tab: DealTab) => void
}

export function DealTabs({ value, counts, onChange }: DealTabsProps) {
  return (
    <div className="flex gap-1 p-1 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border self-start">
      {tabs.map(t => (
        <button
          key={t.value}
          onClick={() => onChange(t.value)}
          className={[
            'px-3 py-1.5 rounded-md text-xs font-medium transition-all flex items-center gap-1.5',
            value === t.value
              ? 'bg-surface-light-card dark:bg-surface-dark-card text-ink-light-primary dark:text-ink-dark-primary shadow-sm'
              : 'text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary',
          ].join(' ')}
        >
          {t.label}
          <span className={[
            'text-[10px] px-1.5 py-0.5 rounded-full font-semibold',
            value === t.value
              ? 'bg-neon-blue/15 text-neon-blue'
              : 'bg-surface-light-border dark:bg-surface-dark-border text-ink-light-muted dark:text-ink-dark-muted',
          ].join(' ')}>
            {counts[t.value]}
          </span>
        </button>
      ))}
    </div>
  )
}
