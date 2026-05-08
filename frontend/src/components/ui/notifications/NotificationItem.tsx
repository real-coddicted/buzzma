import type { Notification } from '../../../types/NotificationTypes'

interface NotificationItemProps {
  notification: Notification
}

export function NotificationItem({ notification: n }: NotificationItemProps) {
  return (
    <div className={[
      'flex items-start gap-3 px-4 py-4 rounded-xl border transition-colors',
      'border-surface-light-border dark:border-surface-dark-border',
      n.unread
        ? 'bg-neon-blue/5 border-neon-blue/20'
        : 'bg-surface-light-card dark:bg-surface-dark-card hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
    ].join(' ')}>
      {n.unread
        ? <span className="mt-1.5 w-2 h-2 rounded-full bg-neon-blue flex-shrink-0" />
        : <span className="mt-1.5 w-2 h-2 rounded-full bg-transparent flex-shrink-0" />
      }
      <div className="flex-1 min-w-0">
        <p className={['text-xs font-semibold truncate', n.accent].join(' ')}>{n.title}</p>
        <p className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 leading-relaxed">
          {n.message}
        </p>
        <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted mt-1.5">{n.time}</p>
      </div>
    </div>
  )
}
