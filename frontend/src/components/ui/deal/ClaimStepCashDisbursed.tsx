import type { Deal } from '../../../types/DealTypes'
import { IconCurrency } from '../icons'

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between items-center py-3 border-b border-surface-light-border dark:border-surface-dark-border last:border-0">
      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{label}</span>
      <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">{value}</span>
    </div>
  )
}

interface ClaimStepCashDisbursedProps {
  deal: Deal
}

export function ClaimStepCashDisbursed({ deal }: ClaimStepCashDisbursedProps) {
  const payout = deal.claimPayout

  const formattedDate = payout?.disbursedOn
    ? new Date(payout.disbursedOn).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
    : '—'

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-center gap-3 py-6">
        <div className="w-16 h-16 rounded-full bg-neon-green/10 border border-neon-green/30 flex items-center justify-center shadow-neon-green">
          <IconCurrency size={28} className="text-neon-green" />
        </div>
        <p className="text-3xl font-bold text-neon-green">
          {payout?.amount ?? '—'}
        </p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
          Cashback Credited
        </p>
      </div>

      <div>
        <Row label="Disbursed On" value={formattedDate} />
        <Row label="Deal"         value={deal.productName} />
        <Row label="Platform"     value={deal.platformLabel} />
      </div>
    </div>
  )
}
