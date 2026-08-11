import { Card } from '../Card'
import { Loading } from '../Loading'
import { BackButton } from '../BackButton'
import { PaginationToolbar } from '../PaginationToolbar'
import { ClaimsTable } from './ClaimsTable'
import { ClaimRow } from './ClaimRow'
import { formatRupees } from '../../../utils/currency'
import type { MadePayment, PayoutClaim } from '../../../types/UserPayoutsTypes'

interface Props {
  payment: MadePayment | undefined
  claims: PayoutClaim[]
  page: number
  totalPages: number
  loading: boolean
  onPageChange: (p: number) => void
  onBack: () => void
}

export function PaymentClaimsDetail({ payment, claims, page, totalPages, loading, onPageChange, onBack }: Props) {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <BackButton onClick={onBack} />
        <div>
          <h2 className="text-lg font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            {payment?.paidAt ?? 'Payment Detail'}
          </h2>
          {payment && (
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              {payment.paymentMethod} · ₹{formatRupees(payment.totalAmount)}
            </p>
          )}
        </div>
      </div>
      <Card padded={false}>
        <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">Claims in this payment</span>
        </div>
        {loading ? (
          <div className="py-8 flex justify-center"><Loading /></div>
        ) : claims.length === 0 ? (
          <p className="px-4 py-8 text-sm text-center text-ink-light-muted dark:text-ink-dark-muted">No claims found</p>
        ) : (
          <ClaimsTable>
            {claims.map((claim, i) => (
              <ClaimRow key={claim.id} claim={claim} isLast={i === claims.length - 1} />
            ))}
          </ClaimsTable>
        )}
        <PaginationToolbar currentPage={page} totalPages={totalPages} onPageChange={onPageChange} disabled={loading} />
      </Card>
    </div>
  )
}
