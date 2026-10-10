import type { PromotionCategory } from '../../../types'
import { PROMOTION_CATEGORY_LABELS } from '../../../constants/campaigns'
import { PromotionCategoryCard } from './PromotionCategoryCard'
import { IconShoppingBag, IconSmartphone, IconGlobe } from '../icons'

const SELECTABLE_CATEGORIES: PromotionCategory[] = ['ECOMMERCE', 'QUICK_COMMERCE', 'APP_PROMOTION', 'SOCIAL_PAGE_PROMOTION']

/** Shown but not selectable until these categories launch. */
const COMING_SOON_CATEGORIES: PromotionCategory[] = ['APP_PROMOTION', 'SOCIAL_PAGE_PROMOTION']

const CATEGORY_ICONS: Record<PromotionCategory, (props: { size?: number }) => JSX.Element> = {
  ECOMMERCE: IconShoppingBag,
  QUICK_COMMERCE: IconShoppingBag,
  APP_PROMOTION: IconSmartphone,
  SOCIAL_PAGE_PROMOTION: IconGlobe,
}

interface Props {
  value: PromotionCategory
  onChange: (value: PromotionCategory) => void
  disabled?: boolean
}

/** Molecule: the row of PromotionCategoryCard atoms, one per PromotionCategory. */
export function PromotionCategorySelector({ value, onChange, disabled }: Props) {
  return (
    <div className="grid grid-cols-4 gap-3">
      {SELECTABLE_CATEGORIES.map(category => {
        const Icon = CATEGORY_ICONS[category]
        return (
          <PromotionCategoryCard
            key={category}
            icon={<Icon size={20} />}
            label={PROMOTION_CATEGORY_LABELS[category]}
            selected={value === category}
            disabled={disabled}
            comingSoon={COMING_SOON_CATEGORIES.includes(category)}
            onClick={() => onChange(category)}
          />
        )
      })}
    </div>
  )
}
