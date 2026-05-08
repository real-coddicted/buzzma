import type { Deal } from '../../../types/DealTypes'
import { PLATFORM_COLORS, DEAL_TYPE_COLORS } from '../../../constants/deal'
import { ProductThumbnail } from './ProductThumbnail'

function paise(amount: number) {
  return `₹${(amount / 100).toLocaleString('en-IN')}`
}

interface DealSummaryRowProps {
  deal: Deal
  className?: string
}

export function DealSummaryRow({ deal, className = '' }: DealSummaryRowProps) {
  return (
    <div className={['flex items-center gap-4', className].join(' ')}>
      <ProductThumbnail
        src={deal.productImageUrl}
        alt={deal.productName}
        className="w-14 h-14 shrink-0 rounded-xl"
      />
      <div className="min-w-0 flex-1 space-y-1">
        <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary leading-snug truncate">
          {deal.productName}
        </p>
        <div className="flex items-center gap-1.5 flex-wrap">
          <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', PLATFORM_COLORS[deal.platform]].join(' ')}>
            {deal.platformLabel}
          </span>
          <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', DEAL_TYPE_COLORS[deal.dealType]].join(' ')}>
            {deal.dealTypeLabel}
          </span>
        </div>
      </div>
      <span className="text-sm font-bold text-neon-green shrink-0">
        {paise(deal.offeredPricePaise)}
      </span>
    </div>
  )
}
