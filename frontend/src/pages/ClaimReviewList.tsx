import { useEffect, useState } from 'react'
import { ClaimReviewGrid } from '../components/ui/claim-review/ClaimReviewGrid'
import { Toast } from '../components/ui/Toast'
import {
  fetchClaimsToReview,
  submitClaimReview,
  bulkApproveClaimReviews,
  submitBrandClaimReview,
  bulkSubmitBrandClaimReviews,
} from '../api/claimApi'
import { getCurrentUser } from '../api/client'
import { type ClaimReviewFilters, emptyFilters } from '../components/ui/claim-review/filters/ClaimReviewFilterTypes'
import type { ClaimReviewItem } from '../types'

interface ClaimReviewListProps {
  onViewDetails: (claim: ClaimReviewItem) => void
}

export function ClaimReviewList({ onViewDetails }: ClaimReviewListProps) {
  const [claims, setClaims] = useState<ClaimReviewItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [appliedFilters, setAppliedFilters] = useState<ClaimReviewFilters>(emptyFilters)
  const isBrand = getCurrentUser()?.role === 'ROLE_BRAND'

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetchClaimsToReview({
      campaignIds: appliedFilters.campaignIds,
      mediatorIds: appliedFilters.mediatorIds,
      brands: appliedFilters.brands,
      platforms: appliedFilters.platforms,
      reviewStatuses: appliedFilters.reviewStatuses,
    })
      .then(data => { if (!cancelled) setClaims(data) })
      .catch(err => { if (!cancelled) setError((err as Error).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [appliedFilters])

  function mergeUpdated(id: string, updated: ClaimReviewItem) {
    setClaims(prev => prev.map(c => (c.id === id ? { ...c, ...updated, campaignName: c.campaignName, mediatorName: c.mediatorName } : c)))
  }

  function handleApprove(row: ClaimReviewItem, amountApprovedPaise?: number) {
    const request = isBrand
      ? submitBrandClaimReview(row.id, 'APPROVED', amountApprovedPaise)
      : submitClaimReview(row.id, 'APPROVED', undefined, amountApprovedPaise)
    request.then(updated => mergeUpdated(row.id, updated)).catch(err => setError((err as Error).message))
  }

  function handleReject(row: ClaimReviewItem) {
    submitBrandClaimReview(row.id, 'REJECTED')
      .then(updated => mergeUpdated(row.id, updated))
      .catch(err => setError((err as Error).message))
  }

  function handleBulkApprove(selectedClaims: ClaimReviewItem[], approvedAmountsPaise: Record<string, number>): Promise<void> {
    const items = selectedClaims.map(c => ({ claimId: c.id, amountApprovedPaise: approvedAmountsPaise[c.id] }))
    const request = isBrand
      ? bulkSubmitBrandClaimReviews(items.map(i => ({ ...i, decision: 'APPROVED' as const })))
      : bulkApproveClaimReviews(items)
    return request
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
        onBulkApprove={handleBulkApprove}
        onReject={isBrand ? handleReject : undefined}
      />
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}