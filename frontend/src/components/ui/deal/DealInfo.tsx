import type { Deal } from '../../../types/DealTypes'
import { APP_NAME } from '../../../constants/app'
import { PLATFORM_COLORS, DEAL_TYPE_COLORS } from '../../../constants/deal'
import { ProductThumbnail } from './ProductThumbnail'
import { OrderOnPlatformLink } from './OrderOnPlatformLink'
import { CopyableCode } from '../CopyableCode'
import { paiseToRupees, formatRupees } from '../../../utils/currency'

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
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-y-auto flex flex-col">
      <div className="relative h-64">
        <a
          href={deal.productUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="relative h-64 block group"
          aria-label={`Order ${deal.productName} on ${deal.platformLabel}`}
        >
          <ProductThumbnail src={deal.productImageUrl} alt={deal.productName} className="h-full" />
          <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors" />
          {discount > 0 && (
            <span className="absolute top-3 right-3 text-[10px] font-bold px-2.5 py-1 rounded-full bg-neon-red text-white">
              -{discount}%
            </span>
          )}
        </a>
        {deal.code && (
          <span className="absolute top-3 left-3">
            <CopyableCode code={deal.code} />
          </span>
        )}
      </div>

      {/* badges + title + price */}
      <div className="px-5 pt-5 pb-3 space-y-3">
        <div className="flex items-center gap-2 flex-wrap">
          <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', PLATFORM_COLORS[deal.platform]].join(' ')}>
            {deal.platformLabel}
          </span>
          <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', DEAL_TYPE_COLORS[deal.dealType]].join(' ')}>
            {deal.dealTypeLabel}
          </span>
        </div>

        <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary leading-snug">
          {deal.productName}
        </h2>

        <div className="flex items-baseline gap-3">
          <span className="text-2xl font-bold text-neon-green">₹{formatRupees(paiseToRupees(deal.offeredPricePaise))}</span>
          <span className="text-sm text-ink-light-muted dark:text-ink-dark-muted line-through">₹{formatRupees(paiseToRupees(deal.originalPricePaise))}</span>
        </div>

        <OrderOnPlatformLink productUrl={deal.productUrl} platformLabel={deal.platformLabel} />
      </div>

      <div className="px-5 pb-5 space-y-4">
        <div>
          <Row label="Platform"       value={deal.platformLabel} />
          <Row label="Deal Type"      value={deal.dealTypeLabel} />
          {deal.sellerName && <Row label="Seller" value={deal.sellerName} />}
          <Row label="Original Price" value={`₹${formatRupees(paiseToRupees(deal.originalPricePaise))}`} />
          <Row label="Offered Price"  value={`₹${formatRupees(paiseToRupees(deal.offeredPricePaise))}`} />
          <Row label="You Save"       value={`₹${formatRupees(paiseToRupees(deal.originalPricePaise - deal.offeredPricePaise))} (${discount}%)`} />
        </div>

        <div className="pt-2 space-y-3">
          <h4 className="text-xs font-bold uppercase tracking-wider text-ink-light-primary dark:text-ink-dark-primary">
            Terms & Conditions
          </h4>
          <ul className="space-y-2">
            {(deal.termsAndConditions
              ? deal.termsAndConditions.split('\n').map(t => t.trim()).filter(Boolean)
              : [
                  'Purchase must be made through the specified platform using your registered account.',
                  'The offered price is valid only for the duration of the campaign and may be withdrawn at any time.',
                  'Only one claim per user per deal is permitted.',
                  'Order ID must be submitted within 48 hours of purchase.',
                  'Refunded, cancelled, or returned orders are not eligible for deal claims.',
                  'Screenshot of the order confirmation may be required for verification.',
                  `${APP_NAME} reserves the right to reject claims that do not meet the stated criteria.`,
                  'Cashback or commission, if applicable, will be credited within 7–14 business days post verification.',
                ]
            ).map((term, i) => (
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
