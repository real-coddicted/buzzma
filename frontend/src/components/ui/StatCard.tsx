import type { StatCardData, StatCardAccent, StatCardIcon } from '../../types'
import {
  IconMegaphone,
  IconUsers,
  IconCurrency,
  IconChart,
  IconTarget,
  IconBolt,
  IconTrendUp,
  IconTrendDown,
} from './icons'

const accentGradients: Record<StatCardAccent, string> = {
  red:    'from-neon-red/20 to-neon-red/5',
  orange: 'from-neon-orange/20 to-neon-orange/5',
  yellow: 'from-neon-yellow/20 to-neon-yellow/5',
  green:  'from-neon-green/20 to-neon-green/5',
  cyan:   'from-neon-cyan/20 to-neon-cyan/5',
  blue:   'from-neon-blue/20 to-neon-blue/5',
  purple: 'from-neon-purple/20 to-neon-purple/5',
  pink:   'from-neon-pink/20 to-neon-pink/5',
}

const accentIconBg: Record<StatCardAccent, string> = {
  red:    'bg-neon-red/10 text-neon-red border-neon-red/20',
  orange: 'bg-neon-orange/10 text-neon-orange border-neon-orange/20',
  yellow: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/20',
  green:  'bg-neon-green/10 text-neon-green border-neon-green/20',
  cyan:   'bg-neon-cyan/10 text-neon-cyan border-neon-cyan/20',
  blue:   'bg-neon-blue/10 text-neon-blue border-neon-blue/20',
  purple: 'bg-neon-purple/10 text-neon-purple border-neon-purple/20',
  pink:   'bg-neon-pink/10 text-neon-pink border-neon-pink/20',
}

const accentText: Record<StatCardAccent, string> = {
  red:    'text-neon-red',
  orange: 'text-neon-orange',
  yellow: 'text-neon-yellow',
  green:  'text-neon-green',
  cyan:   'text-neon-cyan',
  blue:   'text-neon-blue',
  purple: 'text-neon-purple',
  pink:   'text-neon-pink',
}

const accentBorder: Record<StatCardAccent, string> = {
  red:    'border-neon-red/25 dark:border-neon-red/20',
  orange: 'border-neon-orange/25 dark:border-neon-orange/20',
  yellow: 'border-neon-yellow/25 dark:border-neon-yellow/20',
  green:  'border-neon-green/25 dark:border-neon-green/20',
  cyan:   'border-neon-cyan/25 dark:border-neon-cyan/20',
  blue:   'border-neon-blue/25 dark:border-neon-blue/20',
  purple: 'border-neon-purple/25 dark:border-neon-purple/20',
  pink:   'border-neon-pink/25 dark:border-neon-pink/20',
}

function StatIcon({ icon, accent }: { icon: StatCardIcon; accent: StatCardAccent }) {
  const el = {
    megaphone: <IconMegaphone size={20} />,
    users:     <IconUsers size={20} />,
    currency:  <IconCurrency size={20} />,
    chart:     <IconChart size={20} />,
    target:    <IconTarget size={20} />,
    bolt:      <IconBolt size={20} />,
  }[icon]

  return (
    <div
      className={[
        'w-10 h-10 rounded-xl flex items-center justify-center border',
        accentIconBg[accent],
      ].join(' ')}
    >
      {el}
    </div>
  )
}

interface StatCardProps {
  data: StatCardData
}

export function StatCard({ data }: StatCardProps) {
  const { label, value, subValue, trend, trendValue, accent, icon } = data

  return (
    <div
      className={[
        'relative rounded-xl border p-5 overflow-hidden',
        'bg-surface-light-card dark:bg-surface-dark-card',
        'shadow-card-light dark:shadow-card-dark',
        accentBorder[accent],
        'transition-all duration-150 hover:-translate-y-0.5 hover:shadow-lg',
      ].join(' ')}
    >
      <div
        className={[
          'absolute -top-8 -right-8 w-32 h-32 rounded-full blur-3xl opacity-30 bg-gradient-radial',
          `bg-gradient-to-br ${accentGradients[accent]}`,
        ].join(' ')}
      />
      <div className="relative">
        <div className="flex items-start justify-between mb-4">
          <StatIcon icon={icon} accent={accent} />
          <div
            className={[
              'flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full',
              trend === 'up'
                ? 'bg-neon-green/10 text-neon-green'
                : trend === 'down'
                ? 'bg-neon-red/10 text-neon-red'
                : 'bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-muted dark:text-ink-dark-muted',
            ].join(' ')}
          >
            {trend === 'up' && <IconTrendUp size={12} />}
            {trend === 'down' && <IconTrendDown size={12} />}
            {trendValue}
          </div>
        </div>
        <div className={['text-2xl font-bold tracking-tight mb-0.5', accentText[accent]].join(' ')}>
          {value}
        </div>
        <div className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">
          {label}
        </div>
        {subValue && (
          <div className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {subValue}
          </div>
        )}
      </div>
    </div>
  )
}
