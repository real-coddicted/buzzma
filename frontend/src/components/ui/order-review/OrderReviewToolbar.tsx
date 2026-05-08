import { SearchInput } from '../SearchInput'
import { StatusFilterPills } from '../StatusFilterPills'
import { REVIEW_FILTER_OPTIONS } from './orderReviewConstants'
import type { ReviewStatus } from '../../../types'

interface OrderReviewToolbarProps {
  search: string
  onSearchChange: (value: string) => void
  reviewFilter: ReviewStatus | 'all'
  onReviewFilterChange: (value: ReviewStatus | 'all') => void
}

export function OrderReviewToolbar({
  search,
  onSearchChange,
  reviewFilter,
  onReviewFilterChange,
}: OrderReviewToolbarProps) {
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
