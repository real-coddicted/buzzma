import { useEffect, useState } from 'react'
import { ClaimReviewGrid } from '../components/ui/claim-review/ClaimReviewGrid'
import { Toast } from '../components/ui/Toast'
import { fetchClaimsToReview } from '../api/claimApi'
import type { ClaimReviewItem, ReviewStatus } from '../types'

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

  const pendingCount = claims.filter(r => r.reviewStatus === ('pending' as ReviewStatus)).length
  const inReviewCount = claims.filter(r => r.reviewStatus === ('in-review' as ReviewStatus)).length

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Claim Review
        </h1>
        {loading ? (
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">Loading…</p>
        ) : (
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {claims.length} total orders · {pendingCount} pending · {inReviewCount} in review
          </p>
        )}
      </div>
      <ClaimReviewGrid claims={claims} onViewDetails={onViewDetails} />
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}