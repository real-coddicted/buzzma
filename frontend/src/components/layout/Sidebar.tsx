import { useRef, useState } from 'react'
import { NavItem } from '../ui/NavItem'
import { AccountSubmenu } from '../ui/AccountSubmenu'
import { IconDashboard, IconCampaign, IconUsers, IconBolt, IconFeedback, IconList, IconSettings, IconChart, IconLogout, IconX } from '../ui/icons'
import type { NavPage } from '../../types'

const SIDEBAR_WIDTH_PX = 240
const ACCOUNT_MENU_GAP_PX = 2
const ACCOUNT_MENU_CLOSE_DELAY_MS = 150

interface SidebarProps {
  activePage: NavPage
  onNavigate: (page: NavPage) => void
  isOpen: boolean
  onClose: () => void
}

function Logo() {
  return (
    <div className="flex items-center gap-2.5 px-3 mb-8">
      <div
        className="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-black text-surface-dark-base"
        style={{ background: 'linear-gradient(135deg, #57c7ff 0%, #bd93f9 100%)' }}
      >
        B
      </div>
      <span className="text-base font-bold tracking-tight text-ink-light-primary dark:text-ink-dark-primary">
        Buzzma
      </span>
    </div>
  )
}

function SectionLabel({ label }: { label: string }) {
  return (
    <div className="px-3 mb-1.5 mt-4">
      <span className="text-[10px] font-semibold uppercase tracking-widest text-ink-light-muted dark:text-ink-dark-muted">
        {label}
      </span>
    </div>
  )
}

export function Sidebar({ activePage, onNavigate, isOpen, onClose }: SidebarProps) {
  const [isAccountMenuOpen, setIsAccountMenuOpen] = useState(false)
  const [menuPos, setMenuPos] = useState({ top: 0, left: 0 })
  const triggerRef = useRef<HTMLDivElement>(null)
  const closeTimerRef = useRef<number | null>(null)

  const openMenu = () => {
    if (closeTimerRef.current !== null) {
      window.clearTimeout(closeTimerRef.current)
      closeTimerRef.current = null
    }
    if (triggerRef.current) {
      const rect = triggerRef.current.getBoundingClientRect()
      setMenuPos({ top: rect.top, left: SIDEBAR_WIDTH_PX + ACCOUNT_MENU_GAP_PX })
    }
    setIsAccountMenuOpen(true)
  }

  const scheduleClose = () => {
    if (closeTimerRef.current !== null) {
      window.clearTimeout(closeTimerRef.current)
    }
    closeTimerRef.current = window.setTimeout(() => {
      setIsAccountMenuOpen(false)
      closeTimerRef.current = null
    }, ACCOUNT_MENU_CLOSE_DELAY_MS)
  }

  return (
    <>
      {isOpen && (
        <div
          onClick={onClose}
          className="md:hidden fixed inset-0 z-30 bg-black/50 backdrop-blur-sm animate-fade-in"
          aria-hidden="true"
        />
      )}
      <aside
        className={`fixed left-0 top-0 h-screen w-60 flex flex-col bg-surface-light-raised dark:bg-surface-dark-raised border-r border-surface-light-border dark:border-surface-dark-border z-40 transition-transform duration-200 ease-out md:translate-x-0 ${isOpen ? 'translate-x-0' : '-translate-x-full'}`}
      >
        <button
          onClick={onClose}
          className="md:hidden absolute top-3 right-3 w-8 h-8 flex items-center justify-center rounded-lg text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover"
          aria-label="Close menu"
        >
          <IconX size={18} />
        </button>
      <div className="flex-1 overflow-y-auto px-3 pt-5 pb-4 flex flex-col">
        <Logo />

        <SectionLabel label="Main" />
        <div className="flex flex-col gap-1">
          <NavItem
            icon={<IconDashboard />}
            label="Dashboard"
            active={activePage === 'dashboard'}
            onClick={() => onNavigate('dashboard')}
          />
          <NavItem
            icon={<IconCampaign />}
            label="Campaigns"
            active={activePage === 'campaigns'}
            badge={12}
            onClick={() => onNavigate('campaigns')}
          />
          <NavItem
            icon={<IconUsers />}
            label="Connections"
            active={activePage === 'connections'}
            onClick={() => onNavigate('connections')}
          />
          <NavItem
            icon={<IconList />}
            label="Assignments"
            active={activePage === 'assignments'}
            onClick={() => onNavigate('assignments')}
          />
          <NavItem
            icon={<IconBolt />}
            label="Deals"
            active={activePage === 'deals'}
            onClick={() => onNavigate('deals')}
          />
          <NavItem
            icon={<IconChart />}
            label="Claim Review"
            active={activePage === 'claim-review'}
            onClick={() => onNavigate('claim-review')}
          />
        </div>

        <div className="flex-1" />

        <NavItem
          icon={<IconList />}
          label="My Tickets"
          active={activePage === 'my-tickets'}
          onClick={() => onNavigate('my-tickets')}
        />

        <NavItem
          icon={<IconFeedback />}
          label="Feedback"
          active={activePage === 'feedback'}
          onClick={() => onNavigate('feedback')}
        />

        <div
          ref={triggerRef}
          onMouseEnter={openMenu}
          onMouseLeave={scheduleClose}
        >
          <NavItem
            icon={<IconSettings />}
            label="Account"
            active={false}
            onClick={() => (isAccountMenuOpen ? scheduleClose() : openMenu())}
          />
        </div>

        <button
          onClick={() => window.dispatchEvent(new Event('auth:logout'))}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 text-neon-red border border-transparent hover:bg-neon-red/10 hover:border-neon-red/20"
        >
          <IconLogout />
          <span className="flex-1 text-left">Logout</span>
        </button>
      </div>

      <div className="px-4 py-3 border-t border-surface-light-border dark:border-surface-dark-border">
        <div className="flex items-center gap-3">
          <div
            className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold text-white flex-shrink-0"
            style={{ background: 'linear-gradient(135deg, #ff79c6 0%, #bd93f9 100%)' }}
          >
            A
          </div>
          <div className="min-w-0">
            <div className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary truncate">
              Alex Rivera
            </div>
            <div className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted truncate">
              alex@pulse.io
            </div>
          </div>
          <div className="ml-auto w-2 h-2 rounded-full bg-neon-green flex-shrink-0 shadow-neon-green" />
        </div>
      </div>

      <AccountSubmenu
        activePage={activePage}
        onNavigate={onNavigate}
        isVisible={isAccountMenuOpen}
        top={menuPos.top}
        left={menuPos.left}
        onMouseEnter={openMenu}
        onMouseLeave={scheduleClose}
      />
    </aside>
    </>
  )
}
