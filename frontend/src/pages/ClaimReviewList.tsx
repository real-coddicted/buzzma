import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ClaimReviewGrid } from '../components/ui/claim-review/ClaimReviewGrid'
import { Toast } from '../components/ui/Toast'
import { fetchClaimsToReview, submitClaimReview, bulkApproveClaimReviews } from '../api/claimApi'
import { type ClaimReviewFilters, emptyFilters } from '../components/ui/claim-review/filters/ClaimReviewFilterTypes'
import type { ClaimReviewItem } from '../types'

interface ClaimReviewListProps {
  onViewDetails: (claim: ClaimReviewItem) => void
  onOpenImport: () => void
}

export function ClaimReviewList({ onViewDetails, onOpenImport }: ClaimReviewListProps) {
  const [searchParams] = useSearchParams()
  const seedCampaignId = searchParams.get('campaignId')
  const seedCampaignName = searchParams.get('campaignName')
  const [claims, setClaims] = useState<ClaimReviewItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [appliedFilters, setAppliedFilters] = useState<ClaimReviewFilters>(() =>
    seedCampaignId ? { ...emptyFilters(), campaignIds: new Set([seedCampaignId]) } : emptyFilters()
  )

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetchClaimsToReview({
      campaignIds: appliedFilters.campaignIds,
      mediatorIds: appliedFilters.mediatorIds,
      brands: appliedFilters.brands,
      platforms: appliedFilters.platforms,
      claimStatuses: appliedFilters.claimStatuses,
    })
      .then(data => { if (!cancelled) setClaims(data) })
      .catch(err => { if (!cancelled) setError((err as Error).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [appliedFilters])

  function handleApprove(row: ClaimReviewItem, amountApprovedPaise?: number) {
    submitClaimReview(row.id, 'APPROVED', undefined, amountApprovedPaise)
      .then(updated => {
        setClaims(prev => prev.map(c => (c.id === row.id ? { ...c, ...updated, campaignName: c.campaignName, mediatorName: c.mediatorName } : c)))
      })
      .catch(err => setError((err as Error).message))
  }

  function handleBrandVerify(row: ClaimReviewItem) {
    submitClaimReview(row.id, 'BRAND_VERIFIED')
      .then(updated => {
        setClaims(prev => prev.map(c => (c.id === row.id ? { ...c, ...updated, campaignName: c.campaignName, mediatorName: c.mediatorName } : c)))
      })
      .catch(err => setError((err as Error).message))
  }

  function handleBulkApprove(selectedClaims: ClaimReviewItem[], approvedAmountsPaise: Record<string, number>): Promise<void> {
    return bulkApproveClaimReviews(selectedClaims.map(c => ({ claimId: c.id, amountApprovedPaise: approvedAmountsPaise[c.id] })))
      .then(updatedList => {
        const updatedById = new Map(updatedList.map(u => [u.id, u]))
        setClaims(prev => prev.map(c => {
          const updated = updatedById.get(c.id)
          return updated ? { ...c, ...updated, campaignName: c.campaignName, mediatorName: c.mediatorName } : c
        }))
      })
      .catch(err => setError((err as Error).message))
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Claim Review
        </h1>
      </div>
      <ClaimReviewGrid
        claims={claims}
        loading={loading}
        appliedFilters={appliedFilters}
        onApplyFilters={setAppliedFilters}
        onViewDetails={onViewDetails}
        onApprove={handleApprove}
        onBrandVerify={handleBrandVerify}
        onBulkApprove={handleBulkApprove}
        onOpenImport={onOpenImport}
        initialCampaignOption={seedCampaignId && seedCampaignName ? { value: seedCampaignId, label: seedCampaignName } : undefined}
      />
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}