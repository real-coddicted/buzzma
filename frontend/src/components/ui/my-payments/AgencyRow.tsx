import { IconChevronRight } from '../icons'
import { formatRupees } from '../../../utils/currency'
import type { PendingAgency } from '../../../types/MyPaymentsTypes'

interface AgencyRowProps {
  agency: PendingAgency
  isLast: boolean
  onClick: () => void
}

export function AgencyRow({ agency, isLast, onClick }: AgencyRowProps) {
  return (
    <li
      className={[
        'flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors',
        !isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : '',
      ].join(' ')}
      onClick={onClick}
    >
      <div className="shrink-0 w-8 h-8 rounded-full bg-neon-orange/10 flex items-center justify-center text-xs font-bold text-neon-orange">
        {agency.agencyInitials}
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">{agency.agencyName}</p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{agency.pendingClaimCount} claims pending</p>
      </div>
      <p className="text-sm font-semibold text-neon-orange shrink-0">₹{formatRupees(agency.totalPendingAmount)}</p>
      <IconChevronRight className="text-ink-light-muted dark:text-ink-dark-muted shrink-0" />
    </li>
  )
}
