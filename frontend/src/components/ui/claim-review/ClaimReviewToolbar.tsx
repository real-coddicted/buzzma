import { SearchInput } from '../SearchInput'
import { StatusFilterPills } from '../StatusFilterPills'
import { REVIEW_FILTER_OPTIONS } from './claimReviewConstants'
import type { ReviewStatus } from '../../../types'

interface ClaimReviewToolbarProps {
  search: string
  onSearchChange: (value: string) => void
  reviewFilter: ReviewStatus | 'all'
  onReviewFilterChange: (value: ReviewStatus | 'all') => void
}

export function ClaimReviewToolbar({
  search,
  onSearchChange,
  reviewFilter,
  onReviewFilterChange,
}: ClaimReviewToolbarProps) {
  return (
    <div className="p-4 flex flex-col sm:flex-row gap-3 border-b border-surface-light-border dark:border-surface-dark-border">
      <SearchInput
        value={search}
        onChange={onSearchChange}
        placeholder="Search campaign, order, mediator…"
      />
      <StatusFilterPills
        options={REVIEW_FILTER_OPTIONS}
        value={reviewFilter}
        onChange={onReviewFilterChange}
      />
    </div>
  )
}
