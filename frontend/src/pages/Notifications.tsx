import { useSearchParams } from 'react-router-dom'
import { IconBell } from '../components/ui/icons'
import { NotificationTabs } from '../components/ui/notifications/NotificationTabs'
import { NotificationList } from '../components/ui/notifications/NotificationList'
import type { NotificationTab } from '../components/ui/notifications/NotificationTabs'
import type { Notification } from '../types/NotificationTypes'

interface NotificationsProps {
  notifications: Notification[]
  onMarkAllRead: () => void
  onToggleRead: (id: string) => void
  onTogglePin: (id: string) => void
}

export function Notifications({ notifications, onMarkAllRead, onToggleRead, onTogglePin }: NotificationsProps) {
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab: NotificationTab = (searchParams.get('tab') as NotificationTab) ?? 'unread'

  const counts: Record<NotificationTab, number> = {
    unread: notifications.filter(n => n.unread).length,
    read:   notifications.filter(n => !n.unread).length,
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-neon-blue/10 flex items-center justify-center">
          <IconBell size={20} className="text-neon-blue" />
        </div>
        <div>
          <h1 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary">All Notifications</h1>
          <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
            {counts.unread === 0
              ? "You're all caught up"
              : `${counts.unread} unread notification${counts.unread === 1 ? '' : 's'}`}
          </p>
        </div>
      </div>

      <NotificationTabs value={activeTab} counts={counts} onChange={tab => setSearchParams(tab === 'unread' ? {} : { tab })} onMarkAllRead={onMarkAllRead} />

      <NotificationList
        notifications={notifications}
        activeTab={activeTab}
        onToggleRead={onToggleRead}
        onTogglePin={onTogglePin}
      />
    </div>
  )
}
