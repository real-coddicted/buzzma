import { Card } from '../Card'
import { Loading } from '../Loading'
import { Badge } from '../Badge'
import { BackButton } from '../BackButton'
import { formatRupees } from '../../../utils/currency'
import type { PaymentBatch, PaymentClaim } from '../../../types/MyPaymentsTypes'

interface BatchDetailProps {
  batch: PaymentBatch | undefined
  claims: PaymentClaim[]
  loading: boolean
  onBack: () => void
}

function ClaimsTable({ children }: { children: React.ReactNode }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-surface-light-border dark:border-surface-dark-border">
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">#</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Claim ID</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Campaign</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Brand</th>
            <th className="px-4 py-2 text-right text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Amount</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Status</th>
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  )
}

function ClaimRow({ claim, isLast }: { claim: PaymentClaim; isLast: boolean }) {
  return (
    <tr className={!isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : ''}>
      <td className="px-4 py-3 text-xs text-ink-light-muted dark:text-ink-dark-muted">·</td>
      <td className="px-4 py-3 text-xs font-mono text-ink-light-secondary dark:text-ink-dark-secondary">{claim.claimId}</td>
      <td className="px-4 py-3 text-sm text-ink-light-primary dark:text-ink-dark-primary">{claim.campaignName}</td>
      <td className="px-4 py-3 text-sm text-ink-light-secondary dark:text-ink-dark-secondary">{claim.brandName}</td>
      <td className="px-4 py-3 text-sm font-semibold text-right text-neon-green">₹{formatRupees(claim.transactionAmount)}</td>
      <td className="px-4 py-3"><Badge variant="green">{claim.status}</Badge></td>
    </tr>
  )
}

export function BatchDetail({ batch, claims, loading, onBack }: BatchDetailProps) {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <BackButton onClick={onBack} />
        <div>
          <h2 className="text-lg font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            {batch?.date ?? 'Payment Detail'}
          </h2>
          {batch && (
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              {batch.agencyName} · {batch.paymentMode} · ₹{formatRupees(batch.totalAmount)}
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
              <ClaimRow key={claim.claimId} claim={claim} isLast={i === claims.length - 1} />
            ))}
          </ClaimsTable>
        )}
      </Card>
    </div>
  )
}
