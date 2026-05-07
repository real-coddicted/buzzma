import type { Deal } from '../../../types/DealTypes'
import { PLATFORM_COLORS, DEAL_TYPE_COLORS } from '../../../constants/deal'
import { ProductThumbnail } from './ProductThumbnail'
import { ImageFiller } from '../ImageFiller'

function paise(amount: number) {
  return `₹${(amount / 100).toLocaleString('en-IN')}`
}

interface DealCardProps {
  deal: Deal
  onClick: () => void
}

export function DealCard({ deal, onClick }: DealCardProps) {
  const discount = Math.round((1 - deal.offeredPricePaise / deal.originalPricePaise) * 100)

  return (
    <div onClick={onClick} className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-hidden hover:border-neon-blue/30 transition-colors group cursor-pointer">
      <div className="relative h-44">
        {imgError ? (
          <ImageFiller />
        ) : (
          <ProductThumbnail
            src={deal.productImageUrl}
            alt={deal.productName}
            onError={() => setImgError(true)}
            className="h-full"
          imgClassName="group-hover:scale-105 transition-transform duration-300"
          />
        )}
        {discount > 0 && (
          <span className="absolute top-2.5 right-2.5 text-[10px] font-bold px-2 py-0.5 rounded-full bg-neon-red text-white">
            -{discount}%
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
          <span className="text-base font-bold text-neon-green">
            {paise(deal.offeredPricePaise)}
          </span>
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted line-through">
            {paise(deal.originalPricePaise)}
          </span>
        </div>
      </div>
    </div>
  )
}
