import { useState, useEffect, useMemo } from 'react'
import { DealCard } from '../components/ui/deal/DealCard'
import { DealDetail } from '../components/ui/deal/DealDetail'
import { DealTabs } from '../components/ui/deal/DealTabs'
import type { DealTab } from '../components/ui/deal/DealTabs'
import { DealFilterBar } from '../components/ui/deal/DealFilterBar'
import type { DealTypeFilter, DealPlatformFilter } from '../components/ui/deal/DealFilterBar'
import type { Deal } from '../types/DealTypes'
import { fetchDeals } from '../api/dealApi'

export function Deals() {
  const [deals, setDeals]                   = useState<Deal[]>([])
  const [loading, setLoading]               = useState(true)
  const [selectedDeal, setSelectedDeal]     = useState<Deal | null>(null)
  const [activeTab, setActiveTab]           = useState<DealTab>('explore')
  const [search, setSearch]                 = useState('')
  const [typeFilter, setTypeFilter]         = useState<DealTypeFilter>('all')
  const [platformFilter, setPlatformFilter] = useState<DealPlatformFilter>('all')

  useEffect(() => {
    fetchDeals().then(data => { setDeals(data); setLoading(false) })
  }, [])

  const counts: Record<DealTab, number> = useMemo(() => ({
    explore:     deals.filter(d => d.status === 'explore').length,
    in_progress: deals.filter(d => d.status === 'in_progress').length,
    completed:   deals.filter(d => d.status === 'completed').length,
  }), [deals])

  const filtered = useMemo(() => {
    return deals.filter(d => {
      const matchesTab      = d.status === activeTab
      const matchesType     = typeFilter     === 'all' || d.dealType === typeFilter
      const matchesPlatform = platformFilter === 'all' || d.platform === platformFilter
      const matchesSearch   = d.productName.toLowerCase().includes(search.toLowerCase())
      return matchesTab && matchesType && matchesPlatform && matchesSearch
    })
  }, [deals, activeTab, search, typeFilter, platformFilter])

  if (selectedDeal) {
    return <DealDetail deal={selectedDeal} onBack={() => setSelectedDeal(null)} />
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

      <DealTabs value={activeTab} counts={counts} onChange={setActiveTab} />

      {activeTab === 'explore' && (
        <DealFilterBar
          search={search}
          onSearchChange={setSearch}
          typeFilter={typeFilter}
          platformFilter={platformFilter}
          onTypeChange={setTypeFilter}
          onPlatformChange={setPlatformFilter}
        />
      )}

      {loading ? (
        <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          Loading…
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          No deals match your filters.
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
