import { useState, useEffect } from 'react'
import { SearchInput } from '../SearchInput'
import { StatusFilterPills } from '../StatusFilterPills'
import type { FilterOption } from '../StatusFilterPills'
import { fetchDealTypes } from '../../../api/dealTypeApi'
import { ALL_TYPES_OPTION, DEAL_TYPE_ACTIVE_CLASSES } from '../../../constants/deal'
import type { CampaignType } from '../../../types/AssignmentTypes'

export type AssignmentTypeFilter = CampaignType | 'all'

interface AssignmentFilterBarProps {
  search:         string
  onSearchChange: (value: string)              => void
  typeFilter:     AssignmentTypeFilter
  onTypeChange:   (value: AssignmentTypeFilter) => void
}

export function AssignmentFilterBar({
  search,
  onSearchChange,
  typeFilter,
  onTypeChange,
}: AssignmentFilterBarProps) {
  const [typeOptions, setTypeOptions] = useState<FilterOption<AssignmentTypeFilter>[]>([ALL_TYPES_OPTION])

  useEffect(() => {
    fetchDealTypes().then(data => setTypeOptions([
      ALL_TYPES_OPTION,
      ...data.map(opt => ({ ...opt, activeClass: DEAL_TYPE_ACTIVE_CLASSES[opt.value] })),
    ]))
  }, [])

  return (
    <div className="flex flex-wrap items-center gap-3 justify-between">
      <SearchInput value={search} onChange={onSearchChange} placeholder="Search assignments…" />

      <div className="flex items-center gap-2 ml-auto">
        <span className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
          Type
        </span>
        <StatusFilterPills options={typeOptions} value={typeFilter} onChange={onTypeChange} />
      </div>
    </div>
  )
}
