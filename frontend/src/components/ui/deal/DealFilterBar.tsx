import { useState, useEffect } from 'react'
import { StatusFilterPills } from '../StatusFilterPills'
import type { FilterOption } from '../StatusFilterPills'
import { SearchInput } from '../SearchInput'
import type { DealTypeFilter, DealPlatformFilter } from '../../../types/DealTypes'
import { fetchPlatforms } from '../../../api/platformApi'
import { fetchDealTypes } from '../../../api/dealTypeApi'
import { ALL_TYPES_OPTION, ALL_PLATFORMS_OPTION, DEAL_TYPE_ACTIVE_CLASSES } from '../../../constants/deal'

export type { DealTypeFilter, DealPlatformFilter }

interface DealFilterBarProps {
  search:           string
  onSearchChange:   (value: string)             => void
  typeFilter:       DealTypeFilter
  platformFilter:   DealPlatformFilter
  onTypeChange:     (value: DealTypeFilter)     => void
  onPlatformChange: (value: DealPlatformFilter) => void
}

export function DealFilterBar({
  search,
  onSearchChange,
  typeFilter,
  platformFilter,
  onTypeChange,
  onPlatformChange,
}: DealFilterBarProps) {
  const [typeOptions, setTypeOptions]         = useState<FilterOption<DealTypeFilter>[]>([ALL_TYPES_OPTION])
  const [platformOptions, setPlatformOptions] = useState<FilterOption<DealPlatformFilter>[]>([ALL_PLATFORMS_OPTION])

  useEffect(() => {
    fetchDealTypes().then(data => setTypeOptions([
      ALL_TYPES_OPTION,
      ...data.map(opt => ({ ...opt, activeClass: DEAL_TYPE_ACTIVE_CLASSES[opt.value] })),
    ]))
    fetchPlatforms().then(data => setPlatformOptions([ALL_PLATFORMS_OPTION, ...data]))
  }, [])

  return (
    <div className="flex flex-wrap items-center gap-3 justify-between">
      <SearchInput value={search} onChange={onSearchChange} placeholder="Search deals…" />

      <div className="flex items-center gap-4 ml-auto">
        <div className="flex items-center gap-2">
          <span className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
            Type
          </span>
          <StatusFilterPills options={typeOptions} value={typeFilter} onChange={onTypeChange} />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
            Platform
          </span>
          <select
            value={platformFilter}
            onChange={e => onPlatformChange(e.target.value as DealPlatformFilter)}
            className="text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary px-2.5 py-1.5 outline-none cursor-pointer hover:border-neon-blue/40 transition-colors"
          >
            {platformOptions.map(p => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
        </div>
      </div>
    </div>
  )
}
