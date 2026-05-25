import { useState } from 'react'
import { ClaimReviewList } from './ClaimReviewList'
import { ClaimDetails } from './ClaimDetails'
import type { ClaimReviewItem } from '../types'

export function ClaimReview() {
  const [selected, setSelected] = useState<ClaimReviewItem | null>(null)

  if (selected) {
    return <ClaimDetails claim={selected} onBack={() => setSelected(null)} />
  }

  return <ClaimReviewList onViewDetails={setSelected} />
}