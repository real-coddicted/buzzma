import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { ClaimReviewList } from './ClaimReviewList'
import { ClaimDetails } from './ClaimDetails'
import { ClaimReviewWorksheetImport } from './ClaimReviewWorksheetImport'
import { ClaimReviewWorksheetRows } from './ClaimReviewWorksheetRows'
import type { ClaimReviewItem } from '../types'
import type { ClaimReviewWorksheetResponseDto } from '../api/claimApi'

export function ClaimReview() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const [selected, setSelected] = useState<ClaimReviewItem | null>(null)
  const [selectedWorksheet, setSelectedWorksheet] = useState<ClaimReviewWorksheetResponseDto | null>(null)

  if (searchParams.get('view') === 'detail' && selected) {
    return <ClaimDetails claim={selected} onBack={() => navigate(-1)} />
  }

  if (searchParams.get('view') === 'rows' && selectedWorksheet) {
    return <ClaimReviewWorksheetRows worksheet={selectedWorksheet} onBack={() => navigate(-1)} />
  }

  if (searchParams.get('view') === 'import') {
    return (
      <ClaimReviewWorksheetImport
        onBack={() => navigate(-1)}
        onOpenRows={worksheet => { setSelectedWorksheet(worksheet); setSearchParams({ view: 'rows' }) }}
      />
    )
  }

  return (
    <ClaimReviewList
      onViewDetails={item => { setSelected(item); setSearchParams({ view: 'detail' }) }}
      onOpenImport={() => setSearchParams({ view: 'import' })}
    />
  )
}