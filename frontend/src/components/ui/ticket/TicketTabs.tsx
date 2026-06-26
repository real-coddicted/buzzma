export type TicketTab = 'raised' | 'assigned'

const tabs: { value: TicketTab; label: string }[] = [
  { value: 'raised',   label: 'Raised'   },
  { value: 'assigned', label: 'Assigned' },
]

interface Props {
  value: TicketTab
  onChange: (tab: TicketTab) => void
}

export function TicketTabs({ value, onChange }: Props) {
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
