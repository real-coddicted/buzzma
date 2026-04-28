import { useState } from 'react'
import { ThemeToggle } from '../ui/ThemeToggle'
import { IconBell, IconSearch, IconChevronRight } from '../ui/icons'
import type { Theme, NavPage } from '../../types'

const pageTitles: Record<NavPage, { title: string; subtitle: string }> = {
  dashboard: { title: 'Dashboard', subtitle: 'Welcome back, Alex' },
  campaigns: { title: 'Campaigns', subtitle: 'Manage and monitor your campaigns' },
  feedback:  { title: 'Feedback',  subtitle: 'Share your thoughts on Pulse' },
}

interface TopbarProps {
  theme: Theme
  onToggleTheme: () => void
  activePage: NavPage
}

interface Notification {
  id: string
  title: string
  time: string
  unread: boolean
  accent: string
}

const notifications: Notification[] = [
  { id: 'n1', title: 'Summer Sale 2025 went live', time: '2m ago', unread: true, accent: 'text-neon-green' },
  { id: 'n2', title: 'Conversion goal reached!', time: '18m ago', unread: true, accent: 'text-neon-cyan' },
  { id: 'n3', title: 'Budget threshold hit (85%)', time: '1h ago', unread: false, accent: 'text-neon-orange' },
]

function NotificationPanel({ onClose }: { onClose: () => void }) {
  return (
    <>
      <div className="fixed inset-0 z-40" onClick={onClose} />
      <div className="absolute right-0 top-10 z-50 w-80 rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-xl animate-fade-in overflow-hidden">
        <div className="flex items-center justify-between px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <span className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            Notifications
          </span>
          <span className="text-xs text-neon-blue cursor-pointer hover:underline">Mark all read</span>
        </div>
        <div className="divide-y divide-surface-light-border dark:divide-surface-dark-border max-h-64 overflow-y-auto">
          {notifications.map(n => (
            <div
              key={n.id}
              className={[
                'flex items-start gap-3 px-4 py-3 text-xs transition-colors',
                n.unread
                  ? 'bg-neon-blue/5 dark:bg-neon-blue/5'
                  : 'hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
              ].join(' ')}
            >
              {n.unread && (
                <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-neon-blue flex-shrink-0" />
              )}
              {!n.unread && <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-transparent flex-shrink-0" />}
              <div className="flex-1 min-w-0">
                <p className={['font-medium truncate', n.accent].join(' ')}>{n.title}</p>
                <p className="text-ink-light-muted dark:text-ink-dark-muted mt-0.5">{n.time}</p>
              </div>
            </div>
          ))}
        </div>
        <div className="px-4 py-2.5 border-t border-surface-light-border dark:border-surface-dark-border">
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted cursor-pointer hover:text-neon-blue transition-colors">
            View all notifications →
          </span>
        </div>
      </div>
    </>
  )
}

function ProfilePanel({ onClose }: { onClose: () => void }) {
  return (
    <>
      <div className="fixed inset-0 z-40" onClick={onClose} />
      <div className="absolute right-0 top-10 z-50 w-56 rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-xl animate-fade-in overflow-hidden">
        <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <div className="flex items-center gap-3">
            <div
              className="w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold text-white"
              style={{ background: 'linear-gradient(135deg, #ff79c6 0%, #bd93f9 100%)' }}
            >
              A
            </div>
            <div>
              <p className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">Alex Rivera</p>
              <p className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">Marketing Lead</p>
            </div>
          </div>
        </div>
        {['Profile', 'Account Settings', 'Billing', 'Sign out'].map(item => (
          <button
            key={item}
            className={[
              'w-full text-left px-4 py-2.5 text-xs font-medium transition-colors',
              item === 'Sign out'
                ? 'text-neon-red hover:bg-neon-red/5'
                : 'text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover hover:text-ink-light-primary dark:hover:text-ink-dark-primary',
            ].join(' ')}
          >
            {item}
          </button>
        ))}
      </div>
    </>
  )
}

export function Topbar({ theme, onToggleTheme, activePage }: TopbarProps) {
  const [showNotifications, setShowNotifications] = useState(false)
  const [showProfile, setShowProfile] = useState(false)

  const unreadCount = notifications.filter(n => n.unread).length
  const { title, subtitle } = pageTitles[activePage]

  return (
    <header className="fixed top-0 left-60 right-0 h-16 z-20 flex items-center gap-4 px-6 bg-surface-light-base/80 dark:bg-surface-dark-base/80 backdrop-blur-sm border-b border-surface-light-border dark:border-surface-dark-border">
      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted flex-1 min-w-0">
        <span>Pulse</span>
        <IconChevronRight size={12} />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium">{title}</span>
      </div>

      <div className="hidden md:flex items-center gap-2 bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-1.5 w-48">
        <IconSearch size={14} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
        <input
          type="text"
          placeholder="Search…"
          className="bg-transparent text-xs outline-none flex-1 text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
        />
        <kbd className="hidden lg:block text-[10px] px-1.5 py-0.5 rounded bg-surface-light-border dark:bg-surface-dark-border text-ink-light-muted dark:text-ink-dark-muted font-mono">
          ⌘K
        </kbd>
      </div>

      <div className="flex items-center gap-1">
        <div className="hidden sm:block mr-1">
          <ThemeToggle theme={theme} onToggle={onToggleTheme} />
        </div>

        <div className="relative">
          <button
            onClick={() => { setShowNotifications(p => !p); setShowProfile(false) }}
            className="relative w-9 h-9 flex items-center justify-center rounded-lg text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors"
          >
            <IconBell size={18} />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-neon-red shadow-neon-red" />
            )}
          </button>
          {showNotifications && (
            <NotificationPanel onClose={() => setShowNotifications(false)} />
          )}
        </div>

        <div className="relative">
          <button
            onClick={() => { setShowProfile(p => !p); setShowNotifications(false) }}
            className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
          >
            <div
              className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white"
              style={{ background: 'linear-gradient(135deg, #ff79c6 0%, #bd93f9 100%)' }}
            >
              A
            </div>
            <div className="hidden md:block text-left">
              <p className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary leading-none">
                Alex Rivera
              </p>
              <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted leading-none mt-0.5">
                {subtitle}
              </p>
            </div>
          </button>
          {showProfile && (
            <ProfilePanel onClose={() => setShowProfile(false)} />
          )}
        </div>
      </div>
    </header>
  )
}
