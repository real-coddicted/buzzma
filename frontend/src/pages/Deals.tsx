import { useState, useEffect, useMemo } from 'react'
import { DealCard }          from '../components/ui/deal/DealCard'
import { DealDetail }        from '../components/ui/deal/DealDetail'
import { ClaimedDealCard }   from '../components/ui/deal/ClaimedDealCard'
import { ClaimedDealDetail } from '../components/ui/deal/ClaimedDealDetail'
import { DealTabs }          from '../components/ui/deal/DealTabs'
import type { DealTab }      from '../components/ui/deal/DealTabs'
import { DealFilterBar }     from '../components/ui/deal/DealFilterBar'
import type { DealTypeFilter, DealPlatformFilter } from '../components/ui/deal/DealFilterBar'
import { StatusFilterPills } from '../components/ui/StatusFilterPills'
import type { FilterOption } from '../components/ui/StatusFilterPills'
import type { Deal, ClaimStatus } from '../types/DealTypes'
import { fetchDeals }        from '../api/dealApi'

type ClaimStatusFilter = ClaimStatus | 'all'

const CLAIM_STATUS_OPTIONS: FilterOption<ClaimStatusFilter>[] = [
  { value: 'all',      label: 'All'      },
  { value: 'pending',  label: 'Pending',  activeClass: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/30' },
  { value: 'approved', label: 'Approved', activeClass: 'bg-neon-green/10  text-neon-green  border-neon-green/30'  },
  { value: 'rejected', label: 'Rejected', activeClass: 'bg-neon-red/10    text-neon-red    border-neon-red/30'    },
]

interface ExploreFilters {
  search:         string
  typeFilter:     DealTypeFilter
  platformFilter: DealPlatformFilter
}

interface ClaimedFilters {
  claimStatusFilter: ClaimStatusFilter
}

export function Deals() {
  const [deals, setDeals]               = useState<Deal[]>([])
  const [loading, setLoading]           = useState(true)
  const [selectedDeal, setSelectedDeal] = useState<Deal | null>(null)
  const [activeTab, setActiveTab]       = useState<DealTab>('explore')

  const [exploreFilters, setExploreFilters] = useState<ExploreFilters>({
    search: '', typeFilter: 'all', platformFilter: 'all',
  })
  const [claimedFilters, setClaimedFilters] = useState<ClaimedFilters>({
    claimStatusFilter: 'all',
  })

  useEffect(() => {
    fetchDeals().then(data => { setDeals(data); setLoading(false) })
  }, [])

  function handleTabChange(tab: DealTab) {
    setActiveTab(tab)
    setSelectedDeal(null)
  }

  const counts: Record<DealTab, number> = useMemo(() => ({
    explore:     deals.filter(d => d.status === 'explore').length,
    in_progress: deals.filter(d => d.status === 'in_progress').length,
    completed:   deals.filter(d => d.status === 'completed').length,
  }), [deals])

  const filtered = useMemo(() => {
    const { search, typeFilter, platformFilter } = exploreFilters
    const { claimStatusFilter } = claimedFilters
    return deals.filter(d => {
      const matchesTab         = d.status === activeTab
      const matchesType        = activeTab !== 'explore'      || typeFilter        === 'all' || d.dealType    === typeFilter
      const matchesPlatform    = activeTab !== 'explore'      || platformFilter    === 'all' || d.platform    === platformFilter
      const matchesClaimStatus = activeTab !== 'in_progress'  || claimStatusFilter === 'all' || d.claimStatus === claimStatusFilter
      const matchesSearch      = d.productName.toLowerCase().includes(search.toLowerCase())
      return matchesTab && matchesType && matchesPlatform && matchesClaimStatus && matchesSearch
    })
  }, [deals, activeTab, exploreFilters, claimedFilters])

  if (selectedDeal) {
    return activeTab === 'in_progress'
      ? <ClaimedDealDetail deal={selectedDeal} onBack={() => setSelectedDeal(null)} />
      : <DealDetail        deal={selectedDeal} onBack={() => setSelectedDeal(null)} />
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Deals
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          {filtered.length} of {deals.length} deals
        </p>
      </div>

      <DealTabs value={activeTab} counts={counts} onChange={handleTabChange} />

      {activeTab === 'explore' && (
        <DealFilterBar
          search={exploreFilters.search}
          onSearchChange={v => setExploreFilters(f => ({ ...f, search: v }))}
          typeFilter={exploreFilters.typeFilter}
          platformFilter={exploreFilters.platformFilter}
          onTypeChange={v => setExploreFilters(f => ({ ...f, typeFilter: v }))}
          onPlatformChange={v => setExploreFilters(f => ({ ...f, platformFilter: v }))}
        />
      )}

      {activeTab === 'in_progress' && (
        <StatusFilterPills
          options={CLAIM_STATUS_OPTIONS}
          value={claimedFilters.claimStatusFilter}
          onChange={v => setClaimedFilters(f => ({ ...f, claimStatusFilter: v }))}
        />
      )}

      {loading ? (
        <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          Loading…
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          No deals found.
        </div>
      ) : activeTab === 'in_progress' ? (
        <div className="flex flex-col gap-3">
          {filtered.map(deal => (
            <ClaimedDealCard key={deal.id} deal={deal} onClick={() => setSelectedDeal(deal)} />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
          {filtered.map(deal => (
            <DealCard key={deal.id} deal={deal} onClick={() => setSelectedDeal(deal)} />
          ))}
        </div>
      )}
    </div>
  )
}
