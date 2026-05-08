import type { Notification } from '../../../types/NotificationTypes'

interface NotificationItemProps {
  notification: Notification
}

export function NotificationItem({ notification: n }: NotificationItemProps) {
  return (
    <div className={[
      'flex items-start gap-3 px-4 py-4 transition-colors',
      n.unread
        ? 'bg-neon-blue/5'
        : 'hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
    ].join(' ')}>
      {n.unread
        ? <span className="mt-1.5 w-2 h-2 rounded-full bg-neon-blue flex-shrink-0" />
        : <span className="mt-1.5 w-2 h-2 rounded-full bg-transparent flex-shrink-0" />
      }
      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <p className={['text-xs font-semibold truncate', n.accent].join(' ')}>{n.title}</p>
          <p className="text-[12px] font-semibold text-ink-light-secondary dark:text-ink-dark-secondary flex-shrink-0">{n.time}</p>
        </div>
        <p className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 leading-relaxed">
          {n.message}
        </p>
      </div>
    </div>
  )
}
