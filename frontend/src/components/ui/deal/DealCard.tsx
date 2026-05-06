import type { Deal, Platform, CampaignType } from '../../../types/DealTypes'

const platformColors: Record<Platform, string> = {
  PLATFORM_AMAZON:   'text-neon-orange bg-neon-orange/10 border-neon-orange/25',
  PLATFORM_FLIPKART: 'text-neon-blue   bg-neon-blue/10   border-neon-blue/25',
  PLATFORM_NYKAA:    'text-neon-pink   bg-neon-pink/10   border-neon-pink/25',
  PLATFORM_MYNTRA:   'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

const dealTypeColors: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING:           'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/25',
  CAMPAIGN_TYPE_REVIEW:           'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/25',
  CAMPAIGN_TYPE_ORDER:            'text-neon-green  bg-neon-green/10  border-neon-green/25',
  CAMPAIGN_TYPE_DISCOUNT:         'text-neon-red    bg-neon-red/10    border-neon-red/25',
  CAMPAIGN_TYPE_AGENCY_DISCRETION:'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

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
      {/* Product image */}
      <div className="relative h-44 bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
        <img
          src={deal.productImageUrl}
          alt={deal.productName}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
        {/* Discount badge */}
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
            platformColors[deal.platform],
          ].join(' ')}>
            {deal.platformLabel}
          </span>
          <span className={[
            'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
            dealTypeColors[deal.dealType],
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
