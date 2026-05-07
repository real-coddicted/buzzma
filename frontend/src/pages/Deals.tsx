import { useState, useEffect, useMemo } from 'react'
import { Card } from '../components/ui/Card'
import { DealCard } from '../components/ui/deal/DealCard'
import { ClaimedDealsList } from '../components/ui/deal/ClaimedDealsList'
import { DealDetail } from '../components/ui/deal/DealDetail'
import { ClaimedDealDetail } from '../components/ui/deal/ClaimedDealDetail'
import { DealTabs } from '../components/ui/deal/DealTabs'
import type { DealTab } from '../components/ui/deal/DealTabs'
import { DealFilterBar } from '../components/ui/deal/DealFilterBar'
import type { DealTypeFilter, DealPlatformFilter } from '../components/ui/deal/DealFilterBar'
import type { Deal } from '../types/DealTypes'
import { fetchExploreDeals, fetchExploreDeals } from '../api/dealApi'
import type { ExploreDealsPage } from '../api/dealApi'
import type { ExploreDealsPage } from '../api/dealApi'

export function Deals() {
  const [selectedDeal, setSelectedDeal]         = useState<Deal | null>(null)
  const [selectedClaimed, setSelectedClaimed]   = useState<Deal | null>(null)
  const [activeTab, setActiveTab]           = useState<DealTab>('explore')
  const [search, setSearch]                 = useState('')
  const [typeFilter, setTypeFilter]         = useState<DealTypeFilter>('all')
  const [platformFilter, setPlatformFilter] = useState<DealPlatformFilter>('all')

  const [explorePage, setExplorePage]       = useState<ExploreDealsPage | null>(null)
  const [exploreLoading, setExploreLoading] = useState(true)
  const [currentPage, setCurrentPage]       = useState(1)

  const [explorePage, setExplorePage]       = useState<ExploreDealsPage | null>(null)
  const [exploreLoading, setExploreLoading] = useState(true)
  const [currentPage, setCurrentPage]       = useState(1)

  useEffect(() => {
    setExploreLoading(true)
    fetchExploreDeals(currentPage).then(data => {
      setExplorePage(data)
      setExploreLoading(false)
    })
  }, [currentPage])

  useEffect(() => {
    setExploreLoading(true)
    fetchExploreDeals(currentPage).then(data => {
      setExplorePage(data)
      setExploreLoading(false)
    })
  }, [currentPage])

  const filteredExplore = useMemo(() => {
    if (!explorePage) return []
    return explorePage.items.filter(d => {
      const matchesType     = typeFilter     === 'all' || d.dealType === typeFilter
      const matchesPlatform = platformFilter === 'all' || d.platform === platformFilter
      const matchesSearch   = d.productName.toLowerCase().includes(search.toLowerCase())
      return matchesType && matchesPlatform && matchesSearch
    })
  }, [explorePage, search, typeFilter, platformFilter])

  const counts: Record<DealTab, number> = {
    explore:     explorePage?.total ?? 0,
    in_progress: 5,
  }

  if (selectedClaimed) {
    return <ClaimedDealDetail deal={selectedClaimed} onBack={() => setSelectedClaimed(null)} />
  }

  if (selectedDeal) {
    return <DealDetail deal={selectedDeal} onBack={() => setSelectedDeal(null)} />
  }

  const totalPages = explorePage?.totalPages ?? 1

  const isLoading = activeTab === 'explore' ? exploreLoading : claimedLoading
  const activeDeals = activeTab === 'explore' ? filteredExplore : claimedDeals
  const totalPages = explorePage?.totalPages ?? 1

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Deals
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          {activeTab === 'explore'
            ? explorePage ? `${explorePage.total} deals` : '…'
            : '5 claimed deals'}
        </p>
      </div>

      <DealTabs value={activeTab} counts={counts} onChange={setActiveTab} />

      <Card padded={false}>
        {activeTab === 'explore' && (
          <div className="p-4 border-b border-surface-light-border dark:border-surface-dark-border">
            <DealFilterBar
              search={search}
              onSearchChange={setSearch}
              typeFilter={typeFilter}
              platformFilter={platformFilter}
              onTypeChange={setTypeFilter}
              onPlatformChange={setPlatformFilter}
            />
          </div>
        )}

        <div className="p-4">
          {activeTab === 'in_progress' ? (
            <ClaimedDealsList onSelect={setSelectedClaimed} />
          ) : exploreLoading ? (
            <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
              Loading…
            </div>
          ) : filteredExplore.length === 0 ? (
            <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
              No deals match your filters.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
              {filteredExplore.map(deal => (
                <DealCard key={deal.id} deal={deal} onClick={() => setSelectedDeal(deal)} />
              ))}
            </div>
          )}
        </div>

        {activeTab === 'explore' && totalPages > 1 && (
          <div className="px-4 py-3 border-t border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              Page {currentPage} of {totalPages}
            </span>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setCurrentPage(p => p - 1)}
                disabled={currentPage === 1 || exploreLoading}
                className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40"
              >
                Previous
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(p => (
                <button
                  key={p}
                  onClick={() => setCurrentPage(p)}
                  disabled={exploreLoading}
                  className={[
                    'px-3 py-1.5 text-xs rounded-lg border transition-colors',
                    p === currentPage
                      ? 'bg-neon-blue/10 border-neon-blue/30 text-neon-blue font-semibold'
                      : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover disabled:opacity-40',
                  ].join(' ')}
                >
                  {p}
                </button>
              ))}
              <button
                onClick={() => setCurrentPage(p => p + 1)}
                disabled={currentPage === totalPages || exploreLoading}
                className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </Card>
    </div>
  )
}
