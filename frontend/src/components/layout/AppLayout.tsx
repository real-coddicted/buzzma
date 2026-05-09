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
  return (
    <div className="min-h-screen bg-surface-light-base dark:bg-surface-dark-base text-ink-light-primary dark:text-ink-dark-primary">
      <Sidebar activePage={activePage} onNavigate={onNavigate} />
      <Topbar
        theme={theme}
        onToggleTheme={onToggleTheme}
        activePage={activePage}
        onNavigate={onNavigate}
        notifications={notifications}
      />
      <main className="ml-60 pt-16 min-h-screen">
        <div className="p-6 animate-fade-in">{children}</div>
      </main>
    </div>
  )
}
