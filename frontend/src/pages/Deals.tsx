import { useState, useEffect, useMemo } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
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
import type { components } from '../types/api'
import { fetchExploreDeals, claimResponseToDeal } from '../api/dealApi'
import type { ExploreDealsPage } from '../api/dealApi'
import { fetchRawClaims } from '../api/claimApi'
import { Loading } from '../components/ui/Loading'
import { PaginationToolbar } from '../components/ui/PaginationToolbar'
import { Toast } from '../components/ui/Toast'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']

export function Deals() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const view     = searchParams.get('view')
  const activeTab: DealTab = (searchParams.get('tab') as DealTab) ?? 'explore'

  const [selectedDeal, setSelectedDeal]         = useState<Deal | null>(null)
  const [selectedClaimed, setSelectedClaimed]   = useState<Deal | null>(null)
  const [selectedClaimedResponse, setSelectedClaimedResponse] = useState<ClaimResponseDto | null>(null)
  const [search, setSearch]                 = useState('')
  const [typeFilter, setTypeFilter]         = useState<DealTypeFilter>('all')
  const [platformFilter, setPlatformFilter] = useState<DealPlatformFilter>('all')

  const [explorePage, setExplorePage]       = useState<ExploreDealsPage | null>(null)
  const [exploreLoading, setExploreLoading] = useState(true)
  const [currentPage, setCurrentPage]       = useState(1)

  const [claimedResponses, setClaimedResponses] = useState<ClaimResponseDto[]>([])
  const [claimedLoading, setClaimedLoading]     = useState(true)

  const [toastError, setToastError]         = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchRawClaims()
      .then(data => { if (!cancelled) setClaimedResponses(data) })
      .catch((err: unknown) => { if (!cancelled) setToastError(err instanceof Error ? err.message : 'Failed to load claimed deals.') })
      .finally(() => { if (!cancelled) setClaimedLoading(false) })
    return () => { cancelled = true }
  }, [])

  useEffect(() => {
    let cancelled = false
    setExploreLoading(true)
    fetchExploreDeals(currentPage)
      .then(data => {
        if (cancelled) return
        setExplorePage(data)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        setExplorePage(null)
        setToastError(err instanceof Error ? err.message : 'Failed to load deals.')
      })
      .finally(() => {
        if (!cancelled) setExploreLoading(false)
      })
    return () => { cancelled = true }
  }, [currentPage])

  const claimedDeals = useMemo(() => claimedResponses.map(claimResponseToDeal), [claimedResponses])

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
    explore: explorePage?.total ?? 0,
    claimed: claimedDeals.length,
  }

  function handleClaimedSelect(deal: Deal) {
    setSelectedClaimed(deal)
    setSelectedClaimedResponse(claimedResponses.find(r => r.id === deal.claimId) ?? null)
  }

  function clearSelectedClaimed() {
    setSelectedClaimed(null)
    setSelectedClaimedResponse(null)
  }

  if (selectedClaimed) {
    return (
      <ClaimedDealDetail
        deal={selectedClaimed}
        onBack={clearSelectedClaimed}
        claimResponse={selectedClaimedResponse ?? undefined}
      />
    )
  }

  if (view === 'detail' && selectedDeal) {
    return <DealDetail deal={selectedDeal} onBack={() => navigate(-1)} />
  }

  const totalPages = explorePage?.totalPages ?? 1

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Deals
        </h1>
      </div>

      <DealTabs value={activeTab} counts={counts} onChange={tab => setSearchParams(tab === 'explore' ? {} : { tab })} />

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
          {activeTab === 'claimed' ? (
            <ClaimedDealsList deals={claimedDeals} loading={claimedLoading} onSelect={handleClaimedSelect} />
          ) : exploreLoading ? (
            <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted">
              <Loading size={32} />
            </div>
          ) : filteredExplore.length === 0 ? (
            <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
              No deals match your filters.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
              {filteredExplore.map(deal => (
                <DealCard key={deal.id} deal={deal} onClick={() => { setSelectedDeal(deal); setSearchParams({ view: 'detail', id: deal.id }) }} />
              ))}
            </div>
          )}
        </div>

        {activeTab === 'explore' && (
          <PaginationToolbar
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
            disabled={exploreLoading}
          />
        )}
      </Card>

      {toastError && (
        <Toast
          message={toastError}
          type="error"
          onDismiss={() => setToastError(null)}
        />
      )}
    </div>
  )
}
