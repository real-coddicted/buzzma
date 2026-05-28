interface PaginationToolbarProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  /** When true, disables every button (e.g. while a page load is in flight). */
  disabled?: boolean
}

export function PaginationToolbar({
  currentPage,
  totalPages,
  onPageChange,
  disabled = false,
}: PaginationToolbarProps) {
  if (totalPages <= 1) return null

  return (
    <div className="px-4 py-3 border-t border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
        Page {currentPage} of {totalPages}
      </span>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={disabled || currentPage === 1}
          className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40"
        >
          Previous
        </button>
        {Array.from({ length: totalPages }, (_, i) => i + 1).map(p => (
          <button
            key={p}
            onClick={() => onPageChange(p)}
            disabled={disabled}
            className={[
              'px-3 py-1.5 text-xs rounded-lg border transition-colors disabled:opacity-40',
              p === currentPage
                ? 'bg-neon-blue/10 border-neon-blue/30 text-neon-blue font-semibold'
                : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
            ].join(' ')}
          >
            {p}
          </button>
        ))}
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={disabled || currentPage === totalPages}
          className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40"
        >
          Next
        </button>
      </div>
    </div>
  )
}
