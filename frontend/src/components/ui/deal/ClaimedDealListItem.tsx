import type { Deal } from '../../../types/DealTypes'
import { PLATFORM_COLORS } from '../../../constants/deal'
import { CLAIM_STEPS } from '../../../constants/claimSteps'
import { ProductThumbnail } from './ProductThumbnail'
import { Stepper } from '../Stepper'

function paise(amount: number) {
  return `₹${(amount / 100).toLocaleString('en-IN')}`
}

interface ClaimedDealListItemProps {
  deal: Deal
  currentStep?: number
  onClick?: () => void
}

export function ClaimedDealListItem({ deal, currentStep = 0, onClick }: ClaimedDealListItemProps) {
  return (
    <div
      onClick={onClick}
      className={[
        'flex flex-col sm:flex-row gap-4 p-4 rounded-2xl border',
        'border-surface-light-border dark:border-surface-dark-border',
        'bg-surface-light-card dark:bg-surface-dark-card',
        onClick ? 'cursor-pointer hover:border-neon-blue/30 transition-colors' : '',
      ].join(' ')}
    >
      <ProductThumbnail
        src={deal.productImageUrl}
        alt={deal.productName}
        className="w-full sm:w-20 h-20 shrink-0 rounded-xl"
      />

      <div className="flex-1 min-w-0 space-y-3">
        <div className="flex items-start gap-2 justify-between">
          <div className="min-w-0">
            <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary leading-snug truncate">
              {deal.productName}
            </p>
            <div className="flex items-center gap-1.5 mt-1 flex-wrap">
              <span className={[
                'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
                PLATFORM_COLORS[deal.platform],
              ].join(' ')}>
                {deal.platformLabel}
              </span>
              <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                {deal.dealTypeLabel}
              </span>
            </div>
          </div>
          <span className="text-sm font-bold text-neon-green shrink-0">
            {paise(deal.offeredPricePaise)}
          </span>
        </div>

        <Stepper steps={CLAIM_STEPS} currentStep={currentStep} />
      </div>
    </div>
  )
}
