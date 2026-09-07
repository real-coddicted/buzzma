import type { Deal } from '../../../types/DealTypes'
import { isDealSoldOut } from '../../../types/DealTypes'
import { ProductThumbnail } from './ProductThumbnail'
import { OrderOnPlatformLink } from './OrderOnPlatformLink'
import { CopyableCode } from '../CopyableCode'
import { Badge } from '../Badge'
import { Chip } from '../Chip'
import { Button } from '../Button'
import { IconSparkle, IconClock } from '../icons'
import { paiseToRupees, formatRupees } from '../../../utils/currency'
import { formatShortDate, formatDaysLeft } from '../../../utils/time'

interface DealCardProps {
  deal: Deal
  onClick: () => void
}

export function DealCard({ deal, onClick }: DealCardProps) {
  const soldOut = isDealSoldOut(deal)
  const discount = Math.round((1 - deal.offeredPricePaise / deal.originalPricePaise) * 100)
  const refundPaise = deal.originalPricePaise - deal.offeredPricePaise
  const daysLeft = deal.endDate ? formatDaysLeft(deal.endDate) : null

  return (
    <div
      onClick={soldOut ? undefined : onClick}
      className={[
        'h-full flex flex-col rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-hidden transition-colors group',
        soldOut ? 'opacity-50 grayscale cursor-not-allowed' : 'hover:border-neon-blue/30 cursor-pointer',
      ].join(' ')}
    >
      <div className="relative h-44">
        <ProductThumbnail
          src={deal.productImageUrl}
          alt={deal.productName}
          className="h-full"
          imgClassName="group-hover:scale-105 transition-transform duration-300"
        />
        <div className="absolute top-2 left-2 flex items-center gap-1.5">
          {discount > 0 && (
            <Chip tone="dark" className="text-[10px] font-bold">
              {discount}% CASHBACK
            </Chip>
          )}
          <Chip tone="light" className="text-[10px] font-semibold">
            {deal.platformLabel}
          </Chip>
        </div>
        {daysLeft && (
          <div className="absolute bottom-2 right-2">
            <Chip tone="dark" className="text-[10px] font-semibold gap-1">
              <IconClock size={11} />
              {daysLeft}
            </Chip>
          </div>
        )}
      </div>

      {/* Content */}
      <div className="flex-1 flex flex-col p-4 space-y-3">
        {/* Badges */}
        <div className="flex items-center gap-1.5 flex-wrap">
          {soldOut && <Badge variant="red">Sold out</Badge>}
          {deal.code && (
            <span onClick={e => e.stopPropagation()}>
              <CopyableCode code={deal.code} />
            </span>
          )}
        </div>

        {/* Product name — reserves two lines of height so the content below lines up across cards */}
        <p className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary leading-snug line-clamp-2 min-h-[2.5rem]">
          {deal.productName}
        </p>

        {/* Pricing + footer, pinned to the bottom of the card */}
        <div className="mt-auto space-y-3 pt-3 border-t border-surface-light-border dark:border-surface-dark-border">
          <div className="flex items-start justify-between">
            <div className="flex flex-col text-xs">
              <span className="text-ink-light-muted dark:text-ink-dark-muted line-through">
                MRP ₹{formatRupees(paiseToRupees(deal.originalPricePaise))}
              </span>
              <span className="text-ink-light-muted dark:text-ink-dark-muted">
                You Pay: <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">₹{formatRupees(paiseToRupees(deal.offeredPricePaise))}</span>
              </span>
            </div>
            <div className="flex flex-col items-end text-xs">
              <span className="text-ink-light-muted dark:text-ink-dark-muted">Refund Amount</span>
              <span className="text-lg font-bold text-neon-green">
                ₹{formatRupees(paiseToRupees(refundPaise))}
              </span>
            </div>
          </div>
          {deal.endDate && (
            <div className="flex items-center justify-between text-xs">
              <span className="text-ink-light-muted dark:text-ink-dark-muted">Ends</span>
              <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">{formatShortDate(deal.endDate)}</span>
            </div>
          )}
          {deal.mediatorName && (
            <div className="flex items-center justify-between text-xs">
              <span className="text-ink-light-muted dark:text-ink-dark-muted">Mediator</span>
              <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">{deal.mediatorName}</span>
            </div>
          )}
          <div className="flex items-center justify-between text-xs">
            <span className="text-ink-light-muted dark:text-ink-dark-muted">
              Type: <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">{deal.dealTypeLabel}</span>
            </span>
            {deal.slotsAvailable != null && (
              <span className={deal.slotsAvailable === 0 ? 'font-semibold text-neon-red' : 'text-neon-green'}>
                {deal.slotsAvailable} slots left
              </span>
            )}
          </div>
          <OrderOnPlatformLink productUrl={deal.productUrl} platformLabel={deal.platformLabel} disabled={soldOut} />
          <Button
            variant="primary"
            className="w-full !bg-[#2B6CB0] dark:!bg-[#3182CE] !text-white !shadow-none hover:!bg-[#2C5282] dark:hover:!bg-[#4299E1] hover:!brightness-100"
            leftIcon={<IconSparkle size={14} />}
            disabled={soldOut}
            onClick={e => { e.stopPropagation(); onClick() }}
          >
            Claim Deal
          </Button>
        </div>
      </div>
    </div>
  )
}
