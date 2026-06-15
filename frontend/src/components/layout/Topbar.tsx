import { useNavigate } from 'react-router-dom'
import { APP_NAME } from '../../constants/app'
import { ThemeToggle } from '../ui/ThemeToggle'
import { IconBell, IconChevronRight, IconChevronLeft, IconMenu } from '../ui/icons'
import type { Theme, NavPage, Notification } from '../../types'

const pageTitles: Record<NavPage, { title: string; subtitle: string }> = {
  dashboard:   { title: 'Dashboard',   subtitle: 'Welcome back' },
  campaigns:   { title: 'Campaigns',   subtitle: 'Manage and monitor your campaigns' },
  connections:  { title: 'Connections',  subtitle: 'Manage your brand and agency connections' },
  assignments:  { title: 'Assignments',  subtitle: 'View and manage your assignments' },
  deals:        { title: 'Deals',        subtitle: 'Browse and manage your deals' },
  feedback:        { title: 'Feedback',         subtitle: `Share your thoughts on ${APP_NAME}` },
  profile:         { title: 'Profile',          subtitle: 'Your profile details' },
  'raise-ticket': { title: 'Raise a Ticket', subtitle: 'Report an issue or request support' },
  'my-tickets':   { title: 'My Tickets',     subtitle: 'Track the status of your support tickets' },
  notifications:  { title: 'Notifications',  subtitle: 'All your alerts and updates' },
  'claim-review': { title: 'Claim Review',   subtitle: 'Review and manage your claims' },
  users:           { title: 'Users',            subtitle: 'Manage users and their access' },
  'tickets':       { title: 'Tickets',          subtitle: 'View and action on user tickets' },
}

interface TopbarProps {
  theme: Theme
  onToggleTheme: () => void
  activePage: NavPage
  canGoBack: boolean
  onNavigate: (page: NavPage) => void
  notifications: Notification[]
  onMenuClick: () => void
}


export function Topbar({ theme, onToggleTheme, activePage, canGoBack, onNavigate, notifications, onMenuClick }: TopbarProps) {
  const navigate = useNavigate()
  const unreadCount = notifications.filter(n => n.unread).length
  const { title, subtitle } = pageTitles[activePage]

  return (
    <header className="fixed top-0 left-0 md:left-60 right-0 h-16 z-20 flex items-center gap-2 sm:gap-4 px-3 sm:px-6 bg-surface-light-base/80 dark:bg-surface-dark-base/80 backdrop-blur-sm border-b border-surface-light-border dark:border-surface-dark-border">
      <button
        onClick={onMenuClick}
        className="md:hidden w-9 h-9 flex items-center justify-center rounded-lg text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors flex-shrink-0"
        aria-label="Open menu"
      >
        <IconMenu size={20} />
      </button>

      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted flex-1 min-w-0">
        {canGoBack && (
          <button
            onClick={() => navigate(-1)}
            className="w-7 h-7 flex items-center justify-center rounded-lg border border-neon-yellow text-neon-yellow hover:bg-neon-yellow/10 transition-colors flex-shrink-0"
            aria-label="Go back"
          >
            <IconChevronLeft size={14} />
          </button>
        )}
        <span className="hidden sm:inline">{APP_NAME}</span>
        <IconChevronRight size={12} className="hidden sm:inline" />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium truncate">{title}</span>
      </div>

      <div className="hidden md:block absolute left-[40%] -translate-x-1/2 text-center pointer-events-none">
        <p className="text-xs font-bold text-ink-light-muted dark:text-ink-dark-muted">{subtitle}</p>
      </div>

      <div className="flex items-center gap-1">
        <div className="hidden sm:block mr-1">
          <ThemeToggle theme={theme} onToggle={onToggleTheme} />
        </div>

        <div className="relative">
          <button
            onClick={() => onNavigate('notifications')}
            className="relative w-9 h-9 flex items-center justify-center rounded-lg text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors"
          >
            <IconBell size={18} />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-neon-red shadow-neon-red" />
            )}
          </button>
        </div>
      </div>
    </header>
  )
}
