import { useState, useMemo } from 'react'
import { Card } from '../Card'
import { OrderReviewToolbar } from './OrderReviewToolbar'
import { OrderReviewActions } from './OrderReviewActions'
import { ORDER_STATUS_CONFIG, REVIEW_STATUS_CONFIG, APPROVAL_METHOD_CONFIG, ORDER_REVIEW_COLUMNS } from './orderReviewConstants'
import { orderReviews } from '../../../data/mockData'
import type { OrderReviewItem, OrderStatus, ReviewStatus, ApprovalMethod } from '../../../types'

function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const { label, classes, dot } = ORDER_STATUS_CONFIG[status]
  return (
    <span className={['inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium border', classes].join(' ')}>
      <span className={['w-1.5 h-1.5 rounded-full inline-block flex-shrink-0', dot].join(' ')} />
      {label}
    </span>
  )
}

function ReviewStatusBadge({ status }: { status: ReviewStatus }) {
  const { label, classes, dot } = REVIEW_STATUS_CONFIG[status]
  return (
    <span className={['inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium border', classes].join(' ')}>
      <span className={['w-1.5 h-1.5 rounded-full inline-block flex-shrink-0', dot].join(' ')} />
      {label}
    </span>
  )
}

function ApprovalMethodBadge({ method }: { method: ApprovalMethod }) {
  const { label, classes } = APPROVAL_METHOD_CONFIG[method]
  return (
    <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', classes].join(' ')}>
      {label}
    </span>
  )
}

// --- main grid ---

export function OrderReviewGrid() {
  const [search, setSearch] = useState('')
  const [reviewFilter, setReviewFilter] = useState<ReviewStatus | 'all'>('all')

  const filtered = useMemo(() => {
    return orderReviews.filter(row => {
      const matchReview = reviewFilter === 'all' || row.reviewStatus === reviewFilter
      const q = search.toLowerCase()
      const matchSearch =
        row.campaignName.toLowerCase().includes(q) ||
        row.orderId.toLowerCase().includes(q) ||
        row.mediatorName.toLowerCase().includes(q)
      return matchReview && matchSearch
    })
  }, [search, reviewFilter])

  function handleAction(action: string, row: OrderReviewItem) {
    console.log(action, row.orderId)
  }

  return (
    <Card padded={false}>
      <OrderReviewToolbar
        search={search}
        onSearchChange={setSearch}
        reviewFilter={reviewFilter}
        onReviewFilterChange={setReviewFilter}
      />

      {/* table */}
      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
              {ORDER_REVIEW_COLUMNS.map(col => (
                <th
                  key={col}
                  className={[
                    'px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted',
                    col === 'Actions' ? 'text-right' : 'text-left',
                  ].join(' ')}
                >
                  {col}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={ORDER_REVIEW_COLUMNS.length} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                  No orders match your filter.
                </td>
              </tr>
            ) : (
              filtered.map(row => (
                <tr
                  key={row.id}
                  className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group"
                >
                  <td className="px-5 py-4">
                    <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                      {row.campaignName}
                    </span>
                  </td>
                  <td className="px-5 py-4">
                    <span className="font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
                      {row.orderId}
                    </span>
                    <div className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted mt-0.5 font-mono">
                      {row.orderDate}
                    </div>
                  </td>
                  <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">
                    {row.mediatorName}
                  </td>
                  <td className="px-5 py-4">
                    <OrderStatusBadge status={row.orderStatus} />
                  </td>
                  <td className="px-5 py-4">
                    <ReviewStatusBadge status={row.reviewStatus} />
                  </td>
                  <td className="px-5 py-4">
                    <ApprovalMethodBadge method={row.approvalMethod} />
                  </td>
                  <td className="px-5 py-4 text-center">
                    {row.mediatorVerified
                      ? <span className="text-neon-green text-base" title="Verified">✔</span>
                      : <span className="text-ink-light-muted dark:text-ink-dark-muted text-base" title="Not verified">✗</span>
                    }
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2 min-w-[96px]">
                      <div className="flex-1 h-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
                        <div
                          className={[
                            'h-full rounded-full transition-all',
                            row.matchPct >= 80 ? 'bg-neon-green' :
                            row.matchPct >= 50 ? 'bg-neon-yellow' :
                            'bg-neon-red',
                          ].join(' ')}
                          style={{ width: `${row.matchPct}%` }}
                        />
                      </div>
                      <span className={[
                        'text-[11px] font-semibold tabular-nums w-7 text-right',
                        row.matchPct >= 80 ? 'text-neon-green' :
                        row.matchPct >= 50 ? 'text-neon-yellow' :
                        'text-neon-red',
                      ].join(' ')}>
                        {row.matchPct}%
                      </span>
                    </div>
                  </td>
                  <td className="px-5 py-4">
                    <OrderReviewActions row={row} onAction={handleAction} />
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* footer */}
      <div className="px-5 py-3 border-t border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
        <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
          Showing {filtered.length} of {orderReviews.length} orders
        </span>
        <div className="flex items-center gap-1">
          <button className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40" disabled>
            Previous
          </button>
          <button className="px-3 py-1.5 text-xs rounded-lg bg-neon-blue/10 border border-neon-blue/30 text-neon-blue font-semibold">
            1
          </button>
          <button className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40" disabled>
            Next
          </button>
        </div>
      </div>
    </Card>
  )
}
