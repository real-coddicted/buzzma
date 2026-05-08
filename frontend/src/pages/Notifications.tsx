import { useState, useEffect } from 'react'
import { IconBell } from '../components/ui/icons'
import { NotificationTabs } from '../components/ui/notifications/NotificationTabs'
import { NotificationList } from '../components/ui/notifications/NotificationList'
import { fetchNotifications } from '../api/notificationApi'
import type { NotificationTab } from '../components/ui/notifications/NotificationTabs'
import type { Notification } from '../types/NotificationTypes'

export function Notifications() {
  const [activeTab, setActiveTab]       = useState<NotificationTab>('unread')
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [loading, setLoading]           = useState(true)
  const [error, setError]               = useState(false)

  useEffect(() => {
    fetchNotifications()
      .then(setNotifications)
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

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
          <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">You're all caught up</p>
        </div>
      </div>

      <NotificationTabs value={activeTab} counts={counts} onChange={setActiveTab} />

      {loading ? (
        <div className="flex justify-center py-16 text-sm text-ink-light-muted dark:text-ink-dark-muted">
          Loading…
        </div>
      ) : error ? (
        <div className="flex justify-center py-16 text-sm text-neon-red">
          Failed to load notifications.
        </div>
      ) : (
        <NotificationList notifications={notifications} activeTab={activeTab} />
      )}
    </div>
  )
}
