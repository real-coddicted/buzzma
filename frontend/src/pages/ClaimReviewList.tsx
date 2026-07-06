import { useEffect, useState } from 'react'
import { ClaimReviewGrid } from '../components/ui/claim-review/ClaimReviewGrid'
import { Toast } from '../components/ui/Toast'
import { fetchClaimsToReview, submitClaimReview } from '../api/claimApi'
import type { ClaimReviewItem } from '../types'

interface ClaimReviewListProps {
  onViewDetails: (claim: ClaimReviewItem) => void
}

export function ClaimReviewList({ onViewDetails }: ClaimReviewListProps) {
  const [claims, setClaims] = useState<ClaimReviewItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetchClaimsToReview()
      .then(data => { if (!cancelled) setClaims(data) })
      .catch(err => { if (!cancelled) setError((err as Error).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  function handleApprove(row: ClaimReviewItem) {
    submitClaimReview(row.id, 'APPROVED')
      .then(updated => {
        setClaims(prev => prev.map(c => (c.id === row.id ? { ...c, ...updated, campaignName: c.campaignName, mediatorName: c.mediatorName } : c)))
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
      <ClaimReviewGrid claims={claims} loading={loading} onViewDetails={onViewDetails} onApprove={handleApprove} />
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}