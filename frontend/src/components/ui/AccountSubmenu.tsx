import { IconProfile } from './icons'
import type { NavPage } from '../../types'

interface AccountSubmenuProps {
  activePage: NavPage
  onNavigate: (page: NavPage) => void
  isVisible: boolean
  top: number
  left: number
  onMouseEnter: () => void
  onMouseLeave: () => void
}

export function AccountSubmenu({
  activePage,
  onNavigate,
  isVisible,
  top,
  left,
  onMouseEnter,
  onMouseLeave,
}: AccountSubmenuProps) {
  if (!isVisible) return null

  return (
    <div
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      style={{ top, left }}
      className="fixed w-48 bg-surface-light-card dark:bg-surface-dark-card border border-surface-light-border dark:border-surface-dark-border rounded-md shadow-lg z-50"
    >
      <div className="flex flex-col gap-1 p-1">
        <button
          onClick={() => onNavigate('profile')}
          className={`flex items-center gap-3 px-3 py-2 rounded-md transition-colors text-sm ${
            activePage === 'profile'
              ? 'bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary'
              : 'text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover'
          }`}
        >
          <IconProfile />
          <span>Profile</span>
        </button>

      </div>
    </div>
  )
}
