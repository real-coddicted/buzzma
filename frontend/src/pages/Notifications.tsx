import { useState } from 'react'
import { IconBell } from '../components/ui/icons'
import { NotificationTabs } from '../components/ui/notifications/NotificationTabs'
import type { NotificationTab } from '../components/ui/notifications/NotificationTabs'

export function Notifications() {
  const [activeTab, setActiveTab] = useState<NotificationTab>('unread')

  const counts: Record<NotificationTab, number> = { unread: 0, read: 0 }

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
    </div>
  )
}
