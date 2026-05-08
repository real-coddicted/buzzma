import type { Notification } from '../../../types/NotificationTypes'
import { IconBell } from '../icons'
import { NotificationItem } from './NotificationItem'
import type { NotificationTab } from './NotificationTabs'

interface NotificationListProps {
  notifications: Notification[]
  activeTab: NotificationTab
}

export function NotificationList({ notifications, activeTab }: NotificationListProps) {
  const filtered = notifications.filter(n => activeTab === 'unread' ? n.unread : !n.unread)

  if (filtered.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 gap-4 text-center">
        <div className="w-16 h-16 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover flex items-center justify-center">
          <IconBell size={28} className="text-ink-light-muted dark:text-ink-dark-muted" />
        </div>
        <p className="text-sm font-medium text-ink-light-secondary dark:text-ink-dark-secondary">
          No {activeTab} notifications
        </p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted max-w-xs">
          Campaign updates, goal alerts, and system messages will appear here.
        </p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-2">
      {filtered.map(n => (
        <NotificationItem key={n.id} notification={n} />
      ))}
    </div>
  )
}
