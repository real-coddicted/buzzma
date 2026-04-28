import type { ReactNode } from 'react'

interface NavItemProps {
  icon: ReactNode
  label: string
  active?: boolean
  badge?: string | number
  onClick?: () => void
  collapsed?: boolean
}

export function NavItem({
  icon,
  label,
  active = false,
  badge,
  onClick,
  collapsed = false,
}: NavItemProps) {
  return (
    <button
      onClick={onClick}
      title={collapsed ? label : undefined}
      className={[
        'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 relative group',
        active
          ? 'bg-neon-blue/10 text-neon-blue dark:text-neon-blue border border-neon-blue/20 dark:border-neon-blue/20 shadow-neon-blue/10'
          : 'text-ink-light-secondary dark:text-ink-dark-secondary border border-transparent hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover hover:text-ink-light-primary dark:hover:text-ink-dark-primary',
        collapsed ? 'justify-center' : '',
      ].join(' ')}
    >
      {active && (
        <span className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 bg-neon-blue rounded-r-full" />
      )}
      <span className={active ? 'text-neon-blue' : ''}>{icon}</span>
      {!collapsed && (
        <span className="flex-1 text-left">{label}</span>
      )}
      {!collapsed && badge != null && (
        <span className="ml-auto text-xs bg-neon-purple/15 text-neon-purple border border-neon-purple/25 px-1.5 py-0.5 rounded-full font-semibold">
          {badge}
        </span>
      )}
    </button>
  )
}
