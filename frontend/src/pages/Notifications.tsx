import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { IconBell } from '../components/ui/icons'
import { NotificationTabs } from '../components/ui/notifications/NotificationTabs'
import { NotificationList } from '../components/ui/notifications/NotificationList'
import type { NotificationTab } from '../components/ui/notifications/NotificationTabs'
import type { Notification } from '../types/NotificationTypes'
import { Toast } from '../components/ui/Toast'
import {
  fetchNotificationsPage,
  fetchUnreadNotificationCount,
  markAsRead,
  markAsUnread,
  pinNotification,
  markAllRead as apiMarkAllRead,
} from '../api/notificationApi'

interface NotificationsProps {
  onUnreadCountChange: (count: number) => void
}

export function Notifications({ onUnreadCountChange }: NotificationsProps) {
  const [searchParams, setSearchParams] = useSearchParams()
  const activeTab: NotificationTab = (searchParams.get('tab') as NotificationTab) ?? 'unread'

  const [currentPage, setCurrentPage] = useState(1)
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [totalPages, setTotalPages] = useState(1)
  const [unreadCount, setUnreadCount] = useState(0)
  const [toastError, setToastError] = useState<string | null>(null)

  function reportError(err: unknown, fallback: string) {
    setToastError(err instanceof Error ? err.message : fallback)
  }

  useEffect(() => {
    setCurrentPage(1)
  }, [activeTab])

  function refreshUnreadCount() {
    fetchUnreadNotificationCount()
      .then(count => { setUnreadCount(count); onUnreadCountChange(count) })
      .catch((err: unknown) => reportError(err, 'Failed to load unread count.'))
  }

  useEffect(() => {
    refreshUnreadCount()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    let cancelled = false
    fetchNotificationsPage(activeTab, currentPage)
      .then(data => {
        if (cancelled) return
        setNotifications(data.items)
        setTotalPages(data.totalPages)
      })
      .catch((err: unknown) => { if (!cancelled) reportError(err, 'Failed to load notifications.') })
    return () => { cancelled = true }
  }, [activeTab, currentPage])

  function refetchCurrentPage() {
    return fetchNotificationsPage(activeTab, currentPage)
      .then(data => { setNotifications(data.items); setTotalPages(data.totalPages) })
  }

  function markAllRead() {
    apiMarkAllRead()
      .then(() => { refreshUnreadCount(); return refetchCurrentPage() })
      .catch((err: unknown) => reportError(err, 'Failed to mark all as read.'))
  }

  function toggleRead(id: string) {
    const notification = notifications.find(n => n.id === id)
    if (!notification) return
    const action = notification.unread ? markAsRead : markAsUnread
    action(id)
      .then(() => { refreshUnreadCount(); return refetchCurrentPage() })
      .catch((err: unknown) => reportError(err, 'Failed to update notification.'))
  }

  function togglePin(id: string) {
    pinNotification(id)
      .then(() => refetchCurrentPage())
      .catch((err: unknown) => reportError(err, 'Failed to update notification.'))
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
            {unreadCount === 0
              ? "You're all caught up"
              : `${unreadCount} unread notification${unreadCount === 1 ? '' : 's'}`}
          </p>
        </div>
      </div>

      <NotificationTabs value={activeTab} unreadCount={unreadCount} onChange={tab => setSearchParams(tab === 'unread' ? {} : { tab })} onMarkAllRead={markAllRead} />

      <NotificationList
        notifications={notifications}
        activeTab={activeTab}
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
        onToggleRead={toggleRead}
        onTogglePin={togglePin}
      />

      {toastError && (
        <Toast
          message={toastError}
          type="error"
          onDismiss={() => setToastError(null)}
        />
      )}
    </div>
  )
}
