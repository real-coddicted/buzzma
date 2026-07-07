import { IconCheck } from '../icons'

export type NotificationTab = 'unread' | 'read'

interface NotificationTabsProps {
  value: NotificationTab
  unreadCount: number
  onChange: (tab: NotificationTab) => void
  onMarkAllRead: () => void
}

const tabs: { value: NotificationTab; label: string }[] = [
  { value: 'unread', label: 'Unread' },
  { value: 'read',   label: 'Read'   },
]

export function NotificationTabs({ value, unreadCount, onChange, onMarkAllRead }: NotificationTabsProps) {
  return (
    <div className="flex items-center justify-between">
      <div className="flex gap-1 p-1 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border">
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
          </button>
        ))}
      </div>

      {unreadCount > 0 && (
        <button
          onClick={onMarkAllRead}
          title="Mark all as read"
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-blue hover:bg-neon-blue/5 transition-colors"
        >
          <IconCheck size={13} />
          Mark all read
        </button>
      )}
    </div>
  )
}
