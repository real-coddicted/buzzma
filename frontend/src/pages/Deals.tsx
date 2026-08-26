import { useState, useEffect, useMemo } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Card } from '../components/ui/Card'
import { DealCard } from '../components/ui/deal/DealCard'
import { DealDetail } from '../components/ui/deal/DealDetail'
import { DealFilterBar } from '../components/ui/deal/DealFilterBar'
import type { DealTypeFilter, DealPlatformFilter } from '../components/ui/deal/DealFilterBar'
import type { Deal } from '../types/DealTypes'
import { fetchExploreDeals } from '../api/dealApi'
import type { ExploreDealsPage } from '../api/dealApi'
import { Loading } from '../components/ui/Loading'
import { PaginationToolbar } from '../components/ui/PaginationToolbar'
import { Toast } from '../components/ui/Toast'

export function Deals() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const view     = searchParams.get('view')
  const dealId   = searchParams.get('id')

  const [selectedDeal, setSelectedDeal]     = useState<Deal | null>(null)
  const [search, setSearch]                 = useState('')
  const [typeFilter, setTypeFilter]         = useState<DealTypeFilter>('all')
  const [platformFilter, setPlatformFilter] = useState<DealPlatformFilter>('all')

  const [explorePage, setExplorePage]       = useState<ExploreDealsPage | null>(null)
  const [exploreLoading, setExploreLoading] = useState(true)
  const [currentPage, setCurrentPage]       = useState(1)

  const [toastError, setToastError]         = useState<string | null>(null)

  useEffect(() => {
    if (view !== 'detail' || !dealId || selectedDeal || !explorePage) return
    const deal = explorePage.items.find(d => d.id === dealId)
    if (deal) setSelectedDeal(deal)
  }, [view, dealId, selectedDeal, explorePage])

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

  const filteredExplore = useMemo(() => {
    if (!explorePage) return []
    return explorePage.items.filter(d => {
      const matchesType     = typeFilter     === 'all' || d.dealType === typeFilter
      const matchesPlatform = platformFilter === 'all' || d.platform === platformFilter
      const searchTerm      = search.toLowerCase()
      const matchesSearch   = d.productName.toLowerCase().includes(searchTerm)
        || (d.code?.toLowerCase().includes(searchTerm) ?? false)
      return matchesType && matchesPlatform && matchesSearch
    })
  }, [explorePage, search, typeFilter, platformFilter])

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

      <Card padded={false}>
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

        <div className="p-4">
          {exploreLoading ? (
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

        <PaginationToolbar
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
          disabled={exploreLoading}
        />
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
