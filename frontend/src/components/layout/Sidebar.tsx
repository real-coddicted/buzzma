import { useState } from 'react'
import { NavItem } from '../ui/NavItem'
import { IconDashboard, IconCampaign, IconUsers, IconBolt, IconFeedback, IconList, IconSettings, IconLogout, IconChart, IconProfile, IconCurrency } from '../ui/icons'
import type { NavPage, UserRole } from '../../types'

interface SidebarProps {
  activePage: NavPage
  onNavigate: (page: NavPage) => void
}

// Role-based navigation mapping
const roleNavigationMap: Record<UserRole, NavPage[]> = {
  brand: ['dashboard', 'campaigns', 'connections', 'order-review', 'my-tickets', 'feedback'],
  agency: ['dashboard', 'campaigns', 'connections', 'order-review', 'my-tickets', 'feedback'],
  mediator: ['dashboard', 'assignments', 'connections', 'order-review', 'my-tickets', 'feedback'],
  buyer: ['dashboard', 'deals', 'my-tickets', 'feedback'],
}

function Logo({ selectedRole, onRoleChange }: { selectedRole: UserRole; onRoleChange: (role: UserRole) => void }) {
  return (
    <div className="mb-6">
      <div className="flex items-center gap-2.5 px-3 mb-3">
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
      <div className="px-3">
        <select
          value={selectedRole}
          onChange={(e) => onRoleChange(e.target.value as UserRole)}
          className="w-full px-2 py-1.5 text-sm rounded-md border border-surface-light-border dark:border-surface-dark-border bg-surface-light-base dark:bg-surface-dark-base text-ink-light-primary dark:text-ink-dark-primary focus:outline-none focus:ring-2 focus:ring-neon-blue"
        >
          <option value="brand">Brand</option>
          <option value="agency">Agency</option>
          <option value="mediator">Mediator</option>
          <option value="buyer">Buyer</option>
        </select>
      </div>
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
  const [selectedRole, setSelectedRole] = useState<UserRole>('brand')
  const allowedPages = roleNavigationMap[selectedRole]

  // Helper to check if a page should be shown
  const isPageAllowed = (page: NavPage) => allowedPages.includes(page)

  return (
    <aside className="fixed left-0 top-0 h-screen w-60 flex flex-col bg-surface-light-raised dark:bg-surface-dark-raised border-r border-surface-light-border dark:border-surface-dark-border z-30">
      <div className="flex-1 overflow-y-auto px-3 pt-5 pb-4 flex flex-col">
        <Logo selectedRole={selectedRole} onRoleChange={setSelectedRole} />

        <SectionLabel label="Main" />
        <div className="flex flex-col gap-1">
          {isPageAllowed('dashboard') && (
            <NavItem
              icon={<IconDashboard />}
              label="Dashboard"
              active={activePage === 'dashboard'}
              onClick={() => onNavigate('dashboard')}
            />
          )}
          {isPageAllowed('campaigns') && (
            <NavItem
              icon={<IconCampaign />}
              label="Campaigns"
              active={activePage === 'campaigns'}
              badge={12}
              onClick={() => onNavigate('campaigns')}
            />
          )}
          {isPageAllowed('connections') && (
            <NavItem
              icon={<IconUsers />}
              label="Connections"
              active={activePage === 'connections'}
              onClick={() => onNavigate('connections')}
            />
          )}
          {isPageAllowed('assignments') && (
            <NavItem
              icon={<IconList />}
              label="Assignments"
              active={activePage === 'assignments'}
              onClick={() => onNavigate('assignments')}
            />
          )}
          {isPageAllowed('deals') && (
            <NavItem
              icon={<IconBolt />}
              label="Deals"
              active={activePage === 'deals'}
              onClick={() => onNavigate('deals')}
            />
          )}
          {isPageAllowed('order-review') && (
            <NavItem
              icon={<IconChart />}
              label="Order Review"
              active={activePage === 'order-review'}
              onClick={() => onNavigate('order-review')}
            />
          )}
        </div>

        <div className="flex-1" />

        {isPageAllowed('my-tickets') && (
          <NavItem
            icon={<IconList />}
            label="My Tickets"
            active={activePage === 'my-tickets'}
            onClick={() => onNavigate('my-tickets')}
          />
        )}

        {isPageAllowed('notifications') && (
          <NavItem
            icon={<IconFeedback />}
            label="Notifications"
            active={activePage === 'notifications'}
            onClick={() => onNavigate('notifications')}
          />
        )}

        {isPageAllowed('feedback') && (
          <NavItem
            icon={<IconFeedback />}
            label="Feedback"
            active={activePage === 'feedback'}
            onClick={() => onNavigate('feedback')}
          />
        )}

        <SectionLabel label="Account" />
        <div className="flex flex-col gap-1">
            <NavItem
              icon={<IconProfile />}
              label="Profile"
              active={activePage === 'profile'}
              onClick={() => onNavigate('profile')}
            />
          <NavItem
            icon={<IconCurrency />}
            label="Billing"
            onClick={() => {}}
          />
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
