import type { PromotionCategory } from '../../../types'
import { PROMOTION_CATEGORY_LABELS } from '../../../constants/campaigns'
import { PromotionCategoryCard } from './PromotionCategoryCard'
import { IconShoppingBag, IconSmartphone } from '../icons'

/**
 * QUICK_COMMERCE is a real PromotionCategory but isn't selectable here yet - it has no backing
 * platforms (Blinkit/Zepto aren't in the Platform enum), so offering it would show an empty
 * platform dropdown. Add it back once those platforms exist.
 */
const SELECTABLE_CATEGORIES: PromotionCategory[] = ['ECOMMERCE', 'APP_PROMOTION']

const CATEGORY_ICONS: Record<PromotionCategory, (props: { size?: number }) => JSX.Element> = {
  ECOMMERCE: IconShoppingBag,
  QUICK_COMMERCE: IconShoppingBag,
  APP_PROMOTION: IconSmartphone,
}

interface Props {
  value: PromotionCategory
  onChange: (value: PromotionCategory) => void
  disabled?: boolean
}

/** Molecule: the row of PromotionCategoryCard atoms, one per PromotionCategory. */
export function PromotionCategorySelector({ value, onChange, disabled }: Props) {
  return (
    <div className="grid grid-cols-2 gap-3">
      {SELECTABLE_CATEGORIES.map(category => {
        const Icon = CATEGORY_ICONS[category]
        return (
          <PromotionCategoryCard
            key={category}
            icon={<Icon size={20} />}
            label={PROMOTION_CATEGORY_LABELS[category]}
            selected={value === category}
            disabled={disabled}
            onClick={() => onChange(category)}
          />
        )
      })}
    </div>
  )
}
