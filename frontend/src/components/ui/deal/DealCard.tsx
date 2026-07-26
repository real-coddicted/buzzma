import type { Deal } from '../../../types/DealTypes'
import { PLATFORM_COLORS, DEAL_TYPE_COLORS } from '../../../constants/deal'
import { ProductThumbnail } from './ProductThumbnail'
import { OrderOnPlatformLink } from './OrderOnPlatformLink'
import { CopyableCode } from '../CopyableCode'
import { paiseToRupees, formatRupees } from '../../../utils/currency'

interface DealCardProps {
  deal: Deal
  onClick: () => void
}

export function DealCard({ deal, onClick }: DealCardProps) {
  const discount = Math.round((1 - deal.offeredPricePaise / deal.originalPricePaise) * 100)

  return (
    <div onClick={onClick} className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-hidden hover:border-neon-blue/30 transition-colors group cursor-pointer">
      <div className="relative h-44">
        <ProductThumbnail
          src={deal.productImageUrl}
          alt={deal.productName}
          className="h-full"
          imgClassName="group-hover:scale-105 transition-transform duration-300"
        />
        {deal.code && (
          <span className="absolute top-2.5 left-2.5" onClick={e => e.stopPropagation()}>
            <CopyableCode code={deal.code} />
          </span>
        )}
      </div>

      {/* Content */}
      <div className="p-4 space-y-3">
        {/* Badges */}
        <div className="flex items-center gap-1.5 flex-wrap">
          <span className={[
            'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
            PLATFORM_COLORS[deal.platform],
          ].join(' ')}>
            {deal.platformLabel}
          </span>
          <span className={[
            'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
            DEAL_TYPE_COLORS[deal.dealType],
          ].join(' ')}>
            {deal.dealTypeLabel}
          </span>
        </div>

        {/* Product name */}
        <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary leading-snug line-clamp-2">
          {deal.productName}
        </p>

        {/* Pricing */}
        <div className="flex items-baseline gap-2">
          <span className="text-xs font-bold text-neon-green">
            ₹{formatRupees(paiseToRupees(deal.offeredPricePaise))}
          </span>
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted line-through">
            ₹{formatRupees(paiseToRupees(deal.originalPricePaise))}
          </span>
          {discount > 0 && (
            <span className="text-base font-bold text-neon-red">-{discount}%</span>
          )}
        </div>
        <div className="flex items-center justify-between">
          <OrderOnPlatformLink productUrl={deal.productUrl} platformLabel={deal.platformLabel} />
          {deal.slotsAvailable != null && (
            <span className={`text-base font-semibold ${deal.slotsAvailable === 0 ? 'text-neon-red' : 'text-ink-light-muted dark:text-ink-dark-muted'}`}>
              {deal.slotsAvailable} Left
            </span>
          )}
        </div>
      </div>
    </div>
  )
}
