import { useEffect, useMemo, useState } from 'react'
import type { Deal, DealTypeFilter } from '../../../types/DealTypes'
import { fetchDealTypes } from '../../../api/dealTypeApi'
import { ALL_TYPES_OPTION, DEAL_TYPE_ACTIVE_CLASSES } from '../../../constants/deal'
import { SearchInput } from '../SearchInput'
import { StatusFilterPills } from '../StatusFilterPills'
import type { FilterOption } from '../StatusFilterPills'
import { ClaimedDealListItem } from './ClaimedDealListItem'

interface ClaimedDealsListProps {
  deals: Deal[]
  loading: boolean
  onSelect: (deal: Deal) => void
}

export function ClaimedDealsList({ deals, loading, onSelect }: ClaimedDealsListProps) {
  const [search, setSearch]           = useState('')
  const [typeFilter, setTypeFilter]   = useState<DealTypeFilter>('all')
  const [typeOptions, setTypeOptions] = useState<FilterOption<DealTypeFilter>[]>([ALL_TYPES_OPTION])

  useEffect(() => {
    fetchDealTypes().then(data => setTypeOptions([
      ALL_TYPES_OPTION,
      ...data.map(opt => ({ ...opt, activeClass: DEAL_TYPE_ACTIVE_CLASSES[opt.value] })),
    ]))
  }, [])

  const filtered = useMemo(() => deals.filter(d => {
    const matchesType   = typeFilter === 'all' || d.dealType === typeFilter
    const matchesSearch = d.productName.toLowerCase().includes(search.toLowerCase())
    return matchesType && matchesSearch
  }), [deals, search, typeFilter])

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center gap-3 justify-between">
        <SearchInput value={search} onChange={setSearch} placeholder="Search claimed deals…" />
        <div className="flex items-center gap-2">
          <span className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
            Type
          </span>
          <StatusFilterPills options={typeOptions} value={typeFilter} onChange={setTypeFilter} />
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-16 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          Loading…
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex justify-center py-16 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          {deals.length === 0 ? 'No claimed deals yet.' : 'No deals match your filters.'}
        </div>
      ) : (
        <div className="flex flex-col gap-3 max-h-[32rem] overflow-y-auto pr-1">
          {filtered.map(deal => (
            <ClaimedDealListItem
              key={deal.id}
              deal={deal}
              currentStep={deal.currentStep}
              onClick={() => onSelect(deal)}
            />
          ))}
        </div>
      )}
    </div>
  )
}
