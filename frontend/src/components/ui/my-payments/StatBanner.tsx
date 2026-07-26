import { formatRupees } from '../../../utils/currency'

interface StatBannerProps {
  label: string
  amount: number
  subtitle: string
  rightCount: number
  rightLabel: string
  accent: 'green' | 'orange'
}

export function StatBanner({ label, amount, subtitle, rightCount, rightLabel, accent }: StatBannerProps) {
  const iconBg = accent === 'green' ? 'bg-neon-green/10' : 'bg-neon-orange/10'
  const amountColor = accent === 'green' ? 'text-neon-green' : 'text-neon-orange'
  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-4 flex items-center gap-4">
      <div className={`shrink-0 w-11 h-11 rounded-xl ${iconBg} flex items-center justify-center text-2xl`}>
        💰
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-[11px] font-semibold uppercase tracking-widest text-ink-light-muted dark:text-ink-dark-muted">{label}</p>
        <p className={`text-2xl font-bold leading-tight ${amountColor}`}>₹{formatRupees(amount)}</p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">{subtitle}</p>
      </div>
      <div className="text-right shrink-0">
        <p className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">{rightCount}</p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{rightLabel}</p>
      </div>
    </div>
  )
}
