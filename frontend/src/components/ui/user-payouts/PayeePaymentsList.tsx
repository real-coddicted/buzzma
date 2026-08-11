import { Card } from '../Card'
import { Badge } from '../Badge'
import { Loading } from '../Loading'
import { BackButton } from '../BackButton'
import { PaginationToolbar } from '../PaginationToolbar'
import { ProofThumbnail } from '../ProofThumbnail'
import { IconChevronRight } from '../icons'
import { formatRupees } from '../../../utils/currency'
import type { PaidPayee, MadePayment } from '../../../types/UserPayoutsTypes'

interface Props {
  payee: PaidPayee | undefined
  payments: MadePayment[]
  page: number
  totalPages: number
  loading: boolean
  onPageChange: (p: number) => void
  onBack: () => void
  onOpenPayment: (payment: MadePayment) => void
  onViewProof: (payment: MadePayment) => void
}

export function PayeePaymentsList({
  payee,
  payments,
  page,
  totalPages,
  loading,
  onPageChange,
  onBack,
  onOpenPayment,
  onViewProof,
}: Props) {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <BackButton onClick={onBack} />
        <div>
          <h2 className="text-lg font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            {payee?.name ?? 'Payment History'}
          </h2>
          {payee && (
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              <Badge variant={payee.role === 'Mediator' ? 'blue' : 'green'} textClass="text-[10px] font-semibold">
                {payee.role}
              </Badge>
              <span className="mx-1.5">·</span>
              {payee.paymentCount} payment{payee.paymentCount !== 1 ? 's' : ''} · ₹{formatRupees(payee.totalAmount)} total
            </p>
          )}
        </div>
      </div>

      <Card padded={false}>
        <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">Payments Made</span>
        </div>

        {loading ? (
          <div className="py-8 flex justify-center"><Loading /></div>
        ) : payments.length === 0 ? (
          <p className="px-4 py-8 text-sm text-center text-ink-light-muted dark:text-ink-dark-muted">No payments found</p>
        ) : (
          <ul>
            {payments.map((payment, i) => {
              const isLast = i === payments.length - 1
              return (
                <li
                  key={payment.id}
                  className={[
                    'flex items-center gap-4 px-4 py-3 cursor-pointer hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors',
                    !isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : '',
                  ].join(' ')}
                  onClick={() => onOpenPayment(payment)}
                >
                  <button
                    className="shrink-0 w-12 h-12 rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover flex items-center justify-center overflow-hidden hover:border-neon-blue/40 transition-colors"
                    title="View payment proof"
                    onClick={e => { e.stopPropagation(); onViewProof(payment) }}
                  >
                    <ProofThumbnail storageKey={payment.screenshotStorageKey} />
                  </button>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">{payment.paidAt}</p>
                    <div className="flex items-center gap-2 mt-0.5 flex-wrap">
                      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
                        {payment.claimCount} claim{payment.claimCount !== 1 ? 's' : ''} paid
                      </span>
                      <span className="text-ink-light-muted dark:text-ink-dark-muted text-xs">·</span>
                      <Badge variant="purple">{payment.paymentMethod}</Badge>
                    </div>
                  </div>
                  <p className="text-sm font-bold text-neon-green shrink-0">₹{formatRupees(payment.totalAmount)}</p>
                  <IconChevronRight className="text-ink-light-muted dark:text-ink-dark-muted shrink-0" />
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
