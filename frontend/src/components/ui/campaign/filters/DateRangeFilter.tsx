interface Props {
  startDate: string
  endDate: string
  onStartChange: (date: string) => void
  onEndChange: (date: string) => void
}

export function DateRangeFilter({ startDate, endDate, onStartChange, onEndChange }: Props) {
  const endInvalid = !!startDate && !!endDate && endDate < startDate

  function handleEndChange(date: string) {
    if (startDate && date < startDate) return
    onEndChange(date)
  }

  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">Date Range</p>
      <div className="flex items-center gap-2">
        <input
          type="date"
          value={startDate}
          onChange={e => {
            onStartChange(e.target.value)
            if (endDate && e.target.value > endDate) onEndChange('')
          }}
          className="flex-1 bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-2 text-xs text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/50 transition-colors"
        />
        <span className="text-ink-light-muted dark:text-ink-dark-muted text-xs">→</span>
        <input
          type="date"
          value={endDate}
          min={startDate || undefined}
          onChange={e => handleEndChange(e.target.value)}
          className={['flex-1 bg-surface-light-hover dark:bg-surface-dark-hover border rounded-lg px-3 py-2 text-xs text-ink-light-primary dark:text-ink-dark-primary outline-none transition-colors', endInvalid ? 'border-neon-red/50 focus:border-neon-red' : 'border-surface-light-border dark:border-surface-dark-border focus:border-neon-blue/50'].join(' ')}
        />
      </div>
      {endInvalid && (
        <p className="text-[10px] text-neon-red mt-1">End date must be ≥ start date</p>
      )}
    </div>
  )
}
