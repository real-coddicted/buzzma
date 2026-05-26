import { useState, useCallback } from 'react'
import type { ReactNode } from 'react'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'
import type { Theme, NavPage, Notification } from '../../types'

interface AppLayoutProps {
  children: ReactNode
  theme: Theme
  onToggleTheme: () => void
  activePage: NavPage
  onNavigate: (page: NavPage) => void
  notifications: Notification[]
}

export function AppLayout({
  children,
  theme,
  onToggleTheme,
  activePage,
  onNavigate,
  notifications,
}: AppLayoutProps) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)

  const handleNavigate = useCallback(
    (page: NavPage) => {
      onNavigate(page)
      setIsSidebarOpen(false)
    },
    [onNavigate],
  )

  return (
    <div className="min-h-screen bg-surface-light-base dark:bg-surface-dark-base text-ink-light-primary dark:text-ink-dark-primary">
      <Sidebar
        activePage={activePage}
        onNavigate={handleNavigate}
        isOpen={isSidebarOpen}
        onClose={() => setIsSidebarOpen(false)}
      />
      <Topbar
        theme={theme}
        onToggleTheme={onToggleTheme}
        activePage={activePage}
        onNavigate={handleNavigate}
        notifications={notifications}
        onMenuClick={() => setIsSidebarOpen(true)}
      />
      <main className="md:ml-60 pt-16 min-h-screen">
        <div className="p-4 sm:p-6 animate-fade-in">{children}</div>
      </main>
    </div>
  )
}
