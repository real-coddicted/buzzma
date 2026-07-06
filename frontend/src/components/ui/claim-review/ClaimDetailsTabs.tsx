export type ClaimDetailsTab = 'details' | 'proof'

interface Tab {
  value: ClaimDetailsTab
  label: string
}

const tabs: Tab[] = [
  { value: 'proof',   label: 'Proof'   },
  { value: 'details', label: 'Details' },
]

interface ClaimDetailsTabsProps {
  value: ClaimDetailsTab
  onChange: (tab: ClaimDetailsTab) => void
}

export function ClaimDetailsTabs({ value, onChange }: ClaimDetailsTabsProps) {
  return (
    <div className="flex gap-1 p-1 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border self-start">
      {tabs.map(t => (
        <button
          key={t.value}
          onClick={() => onChange(t.value)}
          className={[
            'px-3 py-1.5 rounded-md text-xs font-medium transition-all',
            value === t.value
              ? 'bg-surface-light-card dark:bg-surface-dark-card text-ink-light-primary dark:text-ink-dark-primary shadow-sm'
              : 'text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary',
          ].join(' ')}
        >
          {t.label}
        </button>
      ))}
    </div>
  )
}