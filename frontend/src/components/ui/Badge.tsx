import type { ReactNode } from 'react'
import type { CampaignStatus, StatCardAccent } from '../../types'

interface BadgeProps {
  children: ReactNode
  variant: StatCardAccent | 'neutral'
}

const variantClasses: Record<StatCardAccent | 'neutral', string> = {
  red:    'bg-neon-red/10 text-neon-red border-neon-red/25',
  orange: 'bg-neon-orange/10 text-neon-orange border-neon-orange/25',
  yellow: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/25',
  green:  'bg-neon-green/10 text-neon-green border-neon-green/25',
  cyan:   'bg-neon-cyan/10 text-neon-cyan border-neon-cyan/25',
  blue:   'bg-neon-blue/10 text-neon-blue border-neon-blue/25',
  purple: 'bg-neon-purple/10 text-neon-purple border-neon-purple/25',
  pink:   'bg-neon-pink/10 text-neon-pink border-neon-pink/25',
  neutral:'bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-secondary dark:text-ink-dark-secondary border-surface-light-border dark:border-surface-dark-border',
}

export function Badge({ children, variant }: BadgeProps) {
  return (
    <span
      className={[
        'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border',
        variantClasses[variant],
      ].join(' ')}
    >
      {children}
    </span>
  )
}

const statusMap: Record<CampaignStatus, { label: string; variant: StatCardAccent | 'neutral'; dot: string }> = {
  active:    { label: 'Active',    variant: 'green',   dot: 'bg-neon-green' },
  paused:    { label: 'Paused',    variant: 'yellow',  dot: 'bg-neon-yellow' },
  completed: { label: 'Completed', variant: 'cyan',    dot: 'bg-neon-cyan' },
  draft:     { label: 'Draft',     variant: 'neutral', dot: 'bg-ink-light-muted dark:bg-ink-dark-muted' },
}

interface StatusBadgeProps {
  status: CampaignStatus
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const { label, variant, dot } = statusMap[status]
  return (
    <Badge variant={variant}>
      <span className={['w-1.5 h-1.5 rounded-full inline-block', dot, status === 'active' ? 'animate-pulse-slow' : ''].join(' ')} />
      {label}
    </Badge>
  )
}
