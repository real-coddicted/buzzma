import { useState, useMemo } from 'react'
import { Card } from '../Card'
import { ClaimReviewToolbar } from './ClaimReviewToolbar'
import { ClaimReviewActions } from './ClaimReviewActions'
import { CLAIM_REVIEW_COLUMNS } from './claimReviewConstants'
import { ReviewStatusCell } from './ReviewStatusCell'
import { ClaimStatusBadge } from './ClaimStatusBadge'
import type { ClaimReviewItem, ReviewStatus } from '../../../types'

// --- main grid ---

interface ClaimReviewGridProps {
  claims: ClaimReviewItem[]
  onViewDetails: (claim: ClaimReviewItem) => void
}

export function ClaimReviewGrid({ claims, onViewDetails }: ClaimReviewGridProps) {
  const [search, setSearch] = useState('')
  const [reviewFilter, setReviewFilter] = useState<ReviewStatus | 'all'>('all')

  const filtered = useMemo(() => {
    return claims.filter(row => {
      const matchReview = reviewFilter === 'all' || row.reviewStatus === reviewFilter
      const q = search.toLowerCase()
      const matchSearch =
        row.campaignName.toLowerCase().includes(q) ||
        row.orderId.toLowerCase().includes(q) ||
        row.mediatorName.toLowerCase().includes(q)
      return matchReview && matchSearch
    })
  }, [claims, search, reviewFilter])

  function handleAction(action: string, row: ClaimReviewItem) {
    if (action === 'details') {
      onViewDetails(row)
      return
    }
    console.log(action, row.orderId)
  }

  return (
    <Card padded={false}>
      <ClaimReviewToolbar
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
              {CLAIM_REVIEW_COLUMNS.map(col => (
                <th
                  key={col}
                  className={[
                    'px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted',
                    col === 'Actions' ? 'text-right' : col === 'Claim Status' || col === 'Review Status' || col === 'Verified' ? 'text-center' : 'text-left',
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
                <td colSpan={CLAIM_REVIEW_COLUMNS.length} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                  No orders match your filter.
                </td>
              </tr>
            ) : (
              filtered.map(row => (
                <tr
                  key={row.id}
                  onClick={() => handleAction('details', row)}
                  className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group cursor-pointer"
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
                    <div className="flex justify-center">
                      <ClaimStatusBadge status={row.claimStatus} />
                    </div>
                  </td>
                  <td className="px-5 py-4">
                    <div className="flex justify-center">
                      <ReviewStatusCell status={row.reviewStatus} approvalMethod={row.approvalMethod} />
                    </div>
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
                  <td className="px-5 py-4" onClick={e => e.stopPropagation()}>
                    <ClaimReviewActions row={row} onAction={handleAction} />
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
          Showing {filtered.length} of {claims.length} orders
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
