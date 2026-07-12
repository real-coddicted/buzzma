import { useState, useEffect } from 'react'
import { IconX } from '../icons'
import { BrandFilter } from './filters/BrandFilter'
import { PlatformFilter } from './filters/PlatformFilter'
import { TypeFilter } from './filters/TypeFilter'
import { StatusFilter } from './filters/StatusFilter'
import { DateRangeFilter } from './filters/DateRangeFilter'
import { type CampaignFilters, emptyFilters, countActiveFilters } from './filters/CampaignFilterTypes'

interface Props {
  open: boolean
  onClose: () => void
  filters: CampaignFilters
  onApply: (filters: CampaignFilters) => void
}

export function CampaignFilterDrawer({ open, onClose, filters, onApply }: Props) {
  const [draft, setDraft] = useState<CampaignFilters>(emptyFilters)

  useEffect(() => {
    if (open) setDraft(filters)
  }, [open]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!open) return
    function onKey(e: KeyboardEvent) { if (e.key === 'Escape') onClose() }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onClose])

  function handleApply() {
    onApply(draft)
    onClose()
  }

  function handleClear() {
    const empty = emptyFilters()
    setDraft(empty)
    onApply(empty)
    onClose()
  }

  if (!open) return null

  return (
    <>
      <div
        className="fixed inset-0 bg-black/40 z-[200]"
        onClick={onClose}
      />
      <div className="fixed right-0 top-0 bottom-0 w-80 z-[201] bg-surface-light-card dark:bg-surface-dark-card border-l border-surface-light-border dark:border-surface-dark-border flex flex-col shadow-2xl">
        <div className="flex items-center justify-between px-5 py-4 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
          <span className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">Filters</span>
          <button onClick={onClose} className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
            <IconX size={15} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-5 py-4 space-y-5">
          <BrandFilter
            value={draft.brand}
            onChange={brand => setDraft(d => ({ ...d, brand }))}
          />
          <PlatformFilter
            selected={draft.platforms}
            onChange={platforms => setDraft(d => ({ ...d, platforms }))}
          />
          <TypeFilter
            selected={draft.types}
            onChange={types => setDraft(d => ({ ...d, types }))}
          />
          <StatusFilter
            selected={draft.statuses}
            onChange={statuses => setDraft(d => ({ ...d, statuses }))}
          />
          <DateRangeFilter
            startDate={draft.startDate}
            endDate={draft.endDate}
            onStartChange={startDate => setDraft(d => ({ ...d, startDate }))}
            onEndChange={endDate => setDraft(d => ({ ...d, endDate }))}
          />
        </div>

        <div className="flex items-center gap-2 px-5 py-4 border-t border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
          <button
            onClick={handleClear}
            className="flex-1 px-4 py-2 rounded-lg text-xs font-medium border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
          >
            Clear All
          </button>
          <button
            onClick={handleApply}
            disabled={countActiveFilters(draft) === 0}
            className="flex-1 px-4 py-2 rounded-lg text-xs font-semibold bg-neon-blue/10 border border-neon-blue/30 text-neon-blue hover:bg-neon-blue/20 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Apply Filters
          </button>
        </div>
      </div>
    </>
  )
}
