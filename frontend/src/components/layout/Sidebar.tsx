import { NavItem } from '../ui/NavItem'
import { IconDashboard, IconCampaign, IconSettings, IconLogout } from '../ui/icons'
import type { NavPage } from '../../types'

interface SidebarProps {
  activePage: NavPage
  onNavigate: (page: NavPage) => void
}

function Logo() {
  return (
    <div className="flex items-center gap-2.5 px-3 mb-8">
      <div
        className="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-black text-surface-dark-base"
        style={{ background: 'linear-gradient(135deg, #57c7ff 0%, #bd93f9 100%)' }}
      >
        P
      </div>
      <span className="text-base font-bold tracking-tight text-ink-light-primary dark:text-ink-dark-primary">
        Pulse
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

export function Sidebar({ activePage, onNavigate }: SidebarProps) {
  return (
    <aside className="fixed left-0 top-0 h-screen w-60 flex flex-col bg-surface-light-raised dark:bg-surface-dark-raised border-r border-surface-light-border dark:border-surface-dark-border z-30">
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
        </div>

        <div className="flex-1" />

        <SectionLabel label="Account" />
        <div className="flex flex-col gap-1">
          <NavItem
            icon={<IconSettings />}
            label="Settings"
            onClick={() => {}}
          />
          <NavItem
            icon={<IconLogout />}
            label="Log out"
            onClick={() => {}}
          />
        </div>
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
    </aside>
  )
}
