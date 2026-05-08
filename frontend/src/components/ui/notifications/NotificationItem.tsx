import type { Notification } from '../../../types/NotificationTypes'
import { IconPin, IconCheck, IconEyeOff } from '../icons'

interface NotificationItemProps {
  notification: Notification
  onToggleRead: (id: string) => void
  onTogglePin: (id: string) => void
}

export function NotificationItem({ notification: n, onToggleRead, onTogglePin }: NotificationItemProps) {
  return (
    <div className={[
      'group flex items-start gap-3 px-4 py-4 transition-colors cursor-pointer',
      n.unread
        ? 'bg-neon-blue/5 hover:bg-neon-blue/10'
        : 'hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
    ].join(' ')}>
      <span className={[
        'mt-1.5 w-2 h-2 rounded-full flex-shrink-0',
        n.unread ? 'bg-neon-blue' : 'bg-transparent',
      ].join(' ')} />

      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-1.5 min-w-0">
            <p className={['text-xs font-semibold truncate', n.accent].join(' ')}>{n.title}</p>
            {n.pinned && (
              <span className="flex-shrink-0 text-neon-blue">
                <IconPin size={11} />
              </span>
            )}
          </div>
          <div className="flex items-center gap-1 flex-shrink-0">
            {/* actions — visible on hover */}
            <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <button
                onClick={e => { e.stopPropagation(); onTogglePin(n.id) }}
                title={n.pinned ? 'Unpin' : 'Pin to top'}
                className={[
                  'w-7 h-7 flex items-center justify-center rounded transition-colors',
                  n.pinned
                    ? 'text-neon-blue hover:text-neon-blue/70'
                    : 'text-neon-orange hover:text-neon-orange/70',
                ].join(' ')}
              >
                <IconPin size={16} />
              </button>
              <button
                onClick={e => { e.stopPropagation(); onToggleRead(n.id) }}
                title={n.unread ? 'Mark as read' : 'Mark as unread'}
                className="w-7 h-7 flex items-center justify-center rounded text-neon-green hover:text-neon-green/70 transition-colors"
              >
                {n.unread ? <IconCheck size={16} /> : <IconEyeOff size={16} />}
              </button>
            </div>
            <p className="text-[12px] font-semibold text-ink-light-secondary dark:text-ink-dark-secondary">{n.time}</p>
          </div>
        </div>
        <p className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 leading-relaxed">
          {n.message}
        </p>
      </div>
    </div>
  )
}
