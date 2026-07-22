import { SearchInput } from '../SearchInput'
import { IconFilter, IconFileSpreadsheet } from '../icons'

interface ClaimReviewToolbarProps {
  search: string
  onSearchChange: (value: string) => void
  activeFilterCount: number
  onOpenFilters: () => void
  onDownloadReport: () => void
  downloadingReport: boolean
}

export function ClaimReviewToolbar({
  search,
  onSearchChange,
  activeFilterCount,
  onOpenFilters,
  onDownloadReport,
  downloadingReport,
}: ClaimReviewToolbarProps) {
  return (
    <div className="p-4 flex flex-col sm:flex-row gap-3 border-b border-surface-light-border dark:border-surface-dark-border">
      <SearchInput
        value={search}
        onChange={onSearchChange}
        placeholder="Search campaign, order, mediator…"
      />
      <button
        onClick={onOpenFilters}
        className={[
          'flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium border transition-colors',
          activeFilterCount > 0
            ? 'bg-neon-blue/10 border-neon-blue/30 text-neon-blue'
            : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
        ].join(' ')}
      >
        <IconFilter size={13} />
        Filters
        {activeFilterCount > 0 && (
          <span className="inline-flex items-center justify-center w-4 h-4 rounded-full bg-neon-blue text-black text-[9px] font-bold">
            {activeFilterCount}
          </span>
        )}
      </button>
      <button
        onClick={onDownloadReport}
        disabled={downloadingReport}
        title="Download report"
        aria-label="Download report"
        className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <IconFileSpreadsheet size={13} />
        {downloadingReport ? 'Downloading…' : 'Export'}
      </button>
    </div>
  )
}
