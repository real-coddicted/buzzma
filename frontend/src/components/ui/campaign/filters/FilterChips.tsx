import { IconX } from '../../icons'

export interface FilterChip {
  key: string
  label: string
  colorClass?: string
  onRemove: () => void
}

interface Props {
  chips: FilterChip[]
  onClearAll: () => void
}

export function FilterChips({ chips, onClearAll }: Props) {
  if (chips.length === 0) return null
  return (
    <div className="px-4 py-2 flex items-center gap-2 flex-wrap border-b border-surface-light-border dark:border-surface-dark-border">
      <span className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">Active:</span>
      {chips.map(chip => (
        <span
          key={chip.key}
          className={['inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium border', chip.colorClass ?? 'bg-neon-blue/10 border-neon-blue/20 text-neon-blue'].join(' ')}
        >
          {chip.label}
          <button onClick={chip.onRemove} className="hover:text-neon-red transition-colors">
            <IconX size={10} />
          </button>
        </span>
      ))}
      <button
        onClick={onClearAll}
        className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-red transition-colors ml-auto"
      >
        Clear all
      </button>
    </div>
  )
}
