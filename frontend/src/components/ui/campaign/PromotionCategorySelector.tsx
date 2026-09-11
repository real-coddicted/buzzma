import type { PromotionCategory } from '../../../types'
import { PROMOTION_CATEGORY_LABELS } from '../../../constants/campaigns'
import { PromotionCategoryCard } from './PromotionCategoryCard'
import { IconShoppingBag, IconBolt, IconSmartphone } from '../icons'

const SELECTABLE_CATEGORIES: PromotionCategory[] = ['ECOMMERCE', 'QUICK_COMMERCE', 'APP_PROMOTION']

const CATEGORY_ICONS: Record<PromotionCategory, (props: { size?: number }) => JSX.Element> = {
  ECOMMERCE: IconShoppingBag,
  QUICK_COMMERCE: IconBolt,
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
    <div className="grid grid-cols-3 gap-3">
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
