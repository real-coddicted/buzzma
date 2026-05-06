import { IconFilter } from './icons'

export interface FilterOption<T extends string> {
  value: T
  label: string
  activeClasses?: string
}

const DEFAULT_ACTIVE = 'bg-neon-blue/10 text-neon-blue border-neon-blue/30'

interface StatusFilterPillsProps<T extends string> {
  options: FilterOption<T>[]
  value: T
  onChange: (value: T) => void
}

export function StatusFilterPills<T extends string>({
  options,
  value,
  onChange,
}: StatusFilterPillsProps<T>) {
  return (
    <div className="flex items-center gap-1.5 flex-wrap">
      <IconFilter size={13} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
      {options.map(opt => (
        <button
          key={opt.value}
          onClick={() => onChange(opt.value)}
          className={[
            'px-3 py-1 rounded-full text-xs font-medium border transition-all',
            value === opt.value
              ? (opt.activeClasses ?? DEFAULT_ACTIVE)
              : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
          ].join(' ')}
        >
          {opt.label}
        </button>
      ))}
    </div>
  )
}
