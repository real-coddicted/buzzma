import type { AssignmentItem } from '../../../types/AssignmentTypes'
import { PLATFORM_COLORS, DEAL_TYPE_COLORS } from '../../../constants/deal'
import { ProductThumbnail } from '../deal/ProductThumbnail'
import { paiseToRupees, formatRupees } from '../../../utils/currency'

interface AssignmentListItemProps {
  item: AssignmentItem
  onClick?: () => void
}

export function AssignmentListItem({ item, onClick }: AssignmentListItemProps) {
  return (
    <div
      onClick={onClick}
      className={[
        'flex items-center gap-4 p-4 rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card transition-colors',
        onClick ? 'cursor-pointer hover:border-neon-blue/30 group' : '',
      ].join(' ')}
    >
      {/* Product image */}
      <ProductThumbnail
        src={item.productImageUrl}
        alt={item.productName}
        className="w-16 h-16 rounded-lg flex-shrink-0"
        imgClassName={onClick ? 'group-hover:scale-105 transition-transform duration-300' : ''}
      />

      {/* Main content */}
      <div className="flex-1 min-w-0 space-y-1.5">
        <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary leading-snug truncate">
          {item.productName}
        </p>

        <div className="flex items-center gap-1.5 flex-wrap">
          <span className={[
            'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
            PLATFORM_COLORS[item.platform],
          ].join(' ')}>
            {item.platformLabel}
          </span>
          <span className={[
            'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
            DEAL_TYPE_COLORS[item.dealType],
          ].join(' ')}>
            {item.dealTypeLabel}
          </span>
        </div>
      </div>

      {/* Right-side stats */}
      <div className="flex flex-col items-end gap-1 flex-shrink-0">
        <span className="text-base font-bold text-neon-green">
          ₹{formatRupees(paiseToRupees(item.offeredPricePaise))}
        </span>
        <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted line-through">
          ₹{formatRupees(paiseToRupees(item.originalPricePaise))}
        </span>
        <span className="text-[11px] font-semibold text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          {item.slotsOffered} {item.slotsOffered === 1 ? 'slot' : 'slots'}
        </span>
      </div>
    </div>
  )
}
