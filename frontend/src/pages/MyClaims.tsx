import { useState, useEffect, useMemo } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Card } from '../components/ui/Card'
import { ClaimedDealsList } from '../components/ui/deal/ClaimedDealsList'
import { ClaimedDealDetail } from '../components/ui/deal/ClaimedDealDetail'
import type { Deal } from '../types/DealTypes'
import type { components } from '../types/api'
import { claimResponseToDeal } from '../api/dealApi'
import { fetchRawClaims } from '../api/claimApi'
import { Toast } from '../components/ui/Toast'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']

export function MyClaims() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const view = searchParams.get('view')

  const [selectedClaimed, setSelectedClaimed] = useState<Deal | null>(null)
  const [selectedClaimedResponse, setSelectedClaimedResponse] = useState<ClaimResponseDto | null>(null)

  const [claimedResponses, setClaimedResponses] = useState<ClaimResponseDto[]>([])
  const [claimedLoading, setClaimedLoading] = useState(true)

  const [toastError, setToastError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    fetchRawClaims()
      .then(data => { if (!cancelled) setClaimedResponses(data) })
      .catch((err: unknown) => { if (!cancelled) setToastError(err instanceof Error ? err.message : 'Failed to load claimed deals.') })
      .finally(() => { if (!cancelled) setClaimedLoading(false) })
    return () => { cancelled = true }
  }, [])

  const claimedDeals = useMemo(() => claimedResponses.map(claimResponseToDeal), [claimedResponses])

  function handleClaimedSelect(deal: Deal) {
    setSelectedClaimed(deal)
    setSelectedClaimedResponse(claimedResponses.find(r => r.id === deal.claimId) ?? null)
    setSearchParams({ view: 'claimed', id: deal.id })
  }

  if (view === 'claimed' && selectedClaimed) {
    return (
      <ClaimedDealDetail
        deal={selectedClaimed}
        onBack={() => navigate(-1)}
        claimResponse={selectedClaimedResponse ?? undefined}
      />
    )
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          My Claims
        </h1>
      </div>

      <Card padded={false}>
        <div className="p-4">
          <ClaimedDealsList deals={claimedDeals} loading={claimedLoading} onSelect={handleClaimedSelect} />
        </div>
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
