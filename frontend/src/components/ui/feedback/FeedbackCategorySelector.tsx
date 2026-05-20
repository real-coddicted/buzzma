import { CATEGORIES, type FeedbackCategory } from '../../../constants/feedback'

interface FeedbackCategorySelectorProps {
  value: FeedbackCategory | ''
  onChange: (value: FeedbackCategory) => void
}

export function FeedbackCategorySelector({ value, onChange }: FeedbackCategorySelectorProps) {
  return (
    <div className="flex flex-wrap gap-2">
      {CATEGORIES.map(category => (
        <button
          key={category.value}
          type="button"
          onClick={() => onChange(category.value)}
          className={[
            'px-3 py-1.5 rounded-full text-xs font-medium border transition-all',
            value === category.value
              ? 'bg-neon-blue/10 text-neon-blue border-neon-blue/30'
              : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
          ].join(' ')}
        >
          {category.label}
        </button>
      ))}
    </div>
  )
}

