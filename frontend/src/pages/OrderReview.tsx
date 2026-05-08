import { OrderReviewGrid } from '../components/ui/order-review/OrderReviewGrid'
import { orderReviews } from '../data/mockData'

export function OrderReview() {
  const pendingCount = orderReviews.filter(r => r.reviewStatus === 'pending').length
  const inReviewCount = orderReviews.filter(r => r.reviewStatus === 'in-review').length

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Order Review
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          {orderReviews.length} total orders · {pendingCount} pending · {inReviewCount} in review
        </p>
      </div>
      <OrderReviewGrid />
    </div>
  )
}
