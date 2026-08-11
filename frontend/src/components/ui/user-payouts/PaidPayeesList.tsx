import { Card } from '../Card'
import { Badge } from '../Badge'
import { Loading } from '../Loading'
import { PaginationToolbar } from '../PaginationToolbar'
import { StatBanner } from '../my-payments/StatBanner'
import { IconChevronRight } from '../icons'
import { formatRupees } from '../../../utils/currency'
import type { PaidPayee } from '../../../types/UserPayoutsTypes'

interface Props {
  payees: PaidPayee[]
  total: number
  page: number
  totalPages: number
  loading: boolean
  onPageChange: (p: number) => void
  onOpenPayee: (payee: PaidPayee) => void
}

export function PaidPayeesList({ payees, total, page, totalPages, loading, onPageChange, onOpenPayee }: Props) {
  const totalPaid   = payees.reduce((s, p) => s + p.totalAmount, 0)
  const totalClaims = payees.reduce((s, p) => s + p.claimCount, 0)

  return (
    <div className="flex flex-col gap-4">
      <StatBanner
        label="Total Paid Out"
        amount={totalPaid}
        subtitle={`across ${total} user${total !== 1 ? 's' : ''}`}
        rightCount={totalClaims}
        rightLabel="Total Claims"
        accent="green"
      />

      <Card padded={false}>
        <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
          <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">Payment History</span>
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">Click row to view payments</span>
        </div>

        {loading ? (
          <div className="py-8 flex justify-center"><Loading /></div>
        ) : payees.length === 0 ? (
          <p className="px-4 py-12 text-sm text-center text-ink-light-muted dark:text-ink-dark-muted">
            No payments made yet.
          </p>
        ) : (
          <ul>
            {payees.map((payee, i) => {
              const isLast = i === payees.length - 1
              return (
                <li
                  key={payee.id}
                  className={[
                    'flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors',
                    !isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : '',
                  ].join(' ')}
                  onClick={() => onOpenPayee(payee)}
                >
                  <div className="w-9 h-9 rounded-full bg-neon-blue/10 flex items-center justify-center text-xs font-bold text-neon-blue flex-shrink-0">
                    {payee.initials}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">{payee.name}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <Badge variant={payee.role === 'Mediator' ? 'blue' : 'green'} textClass="text-[10px] font-semibold">
                        {payee.role}
                      </Badge>
                      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
                        {payee.paymentCount} payment{payee.paymentCount !== 1 ? 's' : ''} · {payee.claimCount} claim{payee.claimCount !== 1 ? 's' : ''}
                      </span>
                    </div>
                  </div>
                  <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
                    Last paid: {payee.lastPaidDate}
                  </span>
                  <span className="text-sm font-semibold text-neon-green flex-shrink-0">
                    ₹{formatRupees(payee.totalAmount)}
                  </span>
                  <IconChevronRight className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
                </li>
              )
            })}
          </ul>
        )}

        <PaginationToolbar currentPage={page} totalPages={totalPages} onPageChange={onPageChange} disabled={loading} />
      </Card>
    </div>
  )
}
