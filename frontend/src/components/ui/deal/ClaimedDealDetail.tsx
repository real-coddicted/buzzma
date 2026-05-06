import type { Deal } from '../../../types/DealTypes'
import { IconChevronRight } from '../icons'
import { DealInfo }       from './DealInfo'
import { ClaimProgress }  from './ClaimProgress'

interface ClaimedDealDetailProps {
  deal: Deal
  onBack: () => void
}

export function ClaimedDealDetail({ deal, onBack }: ClaimedDealDetailProps) {
  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted">
        <button onClick={onBack} className="hover:text-neon-blue transition-colors">
          Deals
        </button>
        <IconChevronRight size={12} />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium truncate">
          {deal.productName}
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-10rem)]">
        <DealInfo      deal={deal} />
        <ClaimProgress deal={deal} />
      </div>
    </div>
  )
}
