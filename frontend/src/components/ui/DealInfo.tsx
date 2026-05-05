import type { Deal, Platform, CampaignType } from '../../types/DealTypes'

const platformColors: Record<Platform, string> = {
  PLATFORM_AMAZON:   'text-neon-orange bg-neon-orange/10 border-neon-orange/25',
  PLATFORM_FLIPKART: 'text-neon-blue   bg-neon-blue/10   border-neon-blue/25',
  PLATFORM_NYKAA:    'text-neon-pink   bg-neon-pink/10   border-neon-pink/25',
  PLATFORM_MYNTRA:   'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

const dealTypeColors: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING:            'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/25',
  CAMPAIGN_TYPE_REVIEW:            'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/25',
  CAMPAIGN_TYPE_ORDER:             'text-neon-green  bg-neon-green/10  border-neon-green/25',
  CAMPAIGN_TYPE_DISCOUNT:          'text-neon-red    bg-neon-red/10    border-neon-red/25',
  CAMPAIGN_TYPE_AGENCY_DISCRETION: 'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

function paise(amount: number) {
  return `₹${(amount / 100).toLocaleString('en-IN')}`
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between items-center py-3 border-b border-surface-light-border dark:border-surface-dark-border last:border-0">
      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{label}</span>
      <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">{value}</span>
    </div>
  )
}

interface DealInfoProps {
  deal: Deal
}

export function DealInfo({ deal }: DealInfoProps) {
  const discount = Math.round((1 - deal.offeredPricePaise / deal.originalPricePaise) * 100)

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-hidden flex flex-col">
      <div className="relative h-64 flex-shrink-0 bg-surface-light-hover dark:bg-surface-dark-hover">
        <img
          src={deal.productImageUrl}
          alt={deal.productName}
          className="w-full h-full object-cover"
        />
        {discount > 0 && (
          <span className="absolute top-3 right-3 text-[10px] font-bold px-2.5 py-1 rounded-full bg-neon-red text-white">
            -{discount}%
          </span>
        )}
      </div>

      {/* Static: badges + title + price */}
      <div className="px-5 pt-5 pb-3 flex-shrink-0 space-y-3">
        <div className="flex items-center gap-2 flex-wrap">
          <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', platformColors[deal.platform]].join(' ')}>
            {deal.platformLabel}
          </span>
          <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', dealTypeColors[deal.dealType]].join(' ')}>
            {deal.dealTypeLabel}
          </span>
        </div>

        <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary leading-snug">
          {deal.productName}
        </h2>

        <div className="flex items-baseline gap-3">
          <span className="text-2xl font-bold text-neon-green">{paise(deal.offeredPricePaise)}</span>
          <span className="text-sm text-ink-light-muted dark:text-ink-dark-muted line-through">{paise(deal.originalPricePaise)}</span>
        </div>
      </div>

      {/* Scrollable: details table + T&C */}
      <div className="px-5 pb-5 overflow-y-auto space-y-4">
        <div>
          <Row label="Platform"       value={deal.platformLabel} />
          <Row label="Deal Type"      value={deal.dealTypeLabel} />
          <Row label="Original Price" value={paise(deal.originalPricePaise)} />
          <Row label="Offered Price"  value={paise(deal.offeredPricePaise)} />
          <Row label="You Save"       value={`${paise(deal.originalPricePaise - deal.offeredPricePaise)} (${discount}%)`} />
        </div>

        <div className="pt-2 space-y-3">
          <h4 className="text-xs font-bold uppercase tracking-wider text-ink-light-primary dark:text-ink-dark-primary">
            Terms & Conditions
          </h4>
          <ul className="space-y-2">
            {[
              'Purchase must be made through the specified platform using your registered account.',
              'The offered price is valid only for the duration of the campaign and may be withdrawn at any time.',
              'Only one claim per user per deal is permitted.',
              'Order ID must be submitted within 48 hours of purchase.',
              'Refunded, cancelled, or returned orders are not eligible for deal claims.',
              'Screenshot of the order confirmation may be required for verification.',
              'Buzzma reserves the right to reject claims that do not meet the stated criteria.',
              'Cashback or commission, if applicable, will be credited within 7–14 business days post verification.',
            ].map((term, i) => (
              <li key={i} className="flex items-start gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
                <span className="mt-1.5 w-1 h-1 rounded-full bg-ink-light-muted dark:bg-ink-dark-muted flex-shrink-0" />
                {term}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  )
}
