import type { Notification } from '../../../types/NotificationTypes'
import { Card } from '../Card'
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
      <Card>
        <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
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
      </Card>
    )
  }

  return (
    <Card padded={false}>
      <div className="flex flex-col divide-y divide-surface-light-border dark:divide-surface-dark-border max-h-[32rem] overflow-y-auto">
        {filtered.map(n => (
          <NotificationItem key={n.id} notification={n} />
        ))}
      </div>
    </Card>
  )
}
