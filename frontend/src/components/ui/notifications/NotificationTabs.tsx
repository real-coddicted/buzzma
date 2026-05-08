export type NotificationTab = 'unread' | 'read'

interface NotificationTabsProps {
  value: NotificationTab
  counts: Record<NotificationTab, number>
  onChange: (tab: NotificationTab) => void
}

const tabs: { value: NotificationTab; label: string }[] = [
  { value: 'unread', label: 'Unread' },
  { value: 'read',   label: 'Read'   },
]

export function NotificationTabs({ value, counts, onChange }: NotificationTabsProps) {
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
