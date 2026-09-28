import type { ReactNode } from 'react'
import { IconCheck } from '../icons'

interface PromotionCategoryCardProps {
  icon: ReactNode
  label: string
  selected: boolean
  disabled?: boolean
  onClick: () => void
}

/** Atom: a single selectable promotion-category tile (icon + label + selected state). */
export function PromotionCategoryCard({ icon, label, selected, disabled, onClick }: PromotionCategoryCardProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={[
        'relative flex flex-col items-center gap-2 rounded-xl border p-4 text-center transition-all',
        selected
          ? 'border-neon-blue bg-neon-blue/10 text-neon-blue'
          : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
        disabled ? 'opacity-40 cursor-not-allowed' : 'cursor-pointer',
      ].join(' ')}
    >
      {selected && (
        <span className="absolute top-2 right-2">
          <IconCheck size={14} />
        </span>
      )}
      {icon}
      <span className="text-xs font-semibold leading-tight">{label}</span>
    </button>
  )
}
