import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { ClaimReviewList } from './ClaimReviewList'
import { ClaimDetails } from './ClaimDetails'
import type { ClaimReviewItem } from '../types'

export function ClaimReview() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const [selected, setSelected] = useState<ClaimReviewItem | null>(null)

  if (searchParams.get('view') === 'detail' && selected) {
    return <ClaimDetails claim={selected} onBack={() => navigate(-1)} />
  }

  return <ClaimReviewList onViewDetails={item => { setSelected(item); setSearchParams({ view: 'detail' }) }} />
}