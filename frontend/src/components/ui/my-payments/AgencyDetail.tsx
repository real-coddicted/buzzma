import { Card } from '../Card'
import { Loading } from '../Loading'
import { Badge } from '../Badge'
import { BackButton } from '../BackButton'
import { CopyableCode } from '../CopyableCode'
import { formatRupees } from '../../../utils/currency'
import type { PendingAgency, PendingClaim } from '../../../types/MyPaymentsTypes'

interface AgencyDetailProps {
  agency: PendingAgency | undefined
  claims: PendingClaim[]
  loading: boolean
  onBack: () => void
}

function PendingClaimsTable({ children }: { children: React.ReactNode }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-surface-light-border dark:border-surface-dark-border">
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">#</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Order ID</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Campaign</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Submitted</th>
            <th className="px-4 py-2 text-right text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Expected</th>
            <th className="px-4 py-2 text-left text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted">Status</th>
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  )
}

function PendingClaimRow({ claim, isLast }: { claim: PendingClaim; isLast: boolean }) {
  return (
    <tr className={!isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : ''}>
      <td className="px-4 py-3 text-xs text-ink-light-muted dark:text-ink-dark-muted">·</td>
      <td className="px-4 py-3">
        <div className="flex flex-col items-start gap-1">
          <CopyableCode code={claim.claimCode} />
          <span className="text-xs font-mono text-ink-light-secondary dark:text-ink-dark-secondary">{claim.ecommerceOrderId}</span>
        </div>
      </td>
      <td className="px-4 py-3 text-sm text-ink-light-primary dark:text-ink-dark-primary">{claim.campaignName}</td>
      <td className="px-4 py-3 text-xs text-ink-light-muted dark:text-ink-dark-muted">{claim.submittedDate}</td>
      <td className="px-4 py-3 text-sm font-semibold text-right text-neon-orange">₹{formatRupees(claim.expectedAmount)}</td>
      <td className="px-4 py-3"><Badge variant="yellow">{claim.status}</Badge></td>
    </tr>
  )
}

export function AgencyDetail({ agency, claims, loading, onBack }: AgencyDetailProps) {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <BackButton onClick={onBack} />
        <div>
          <h2 className="text-lg font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            {agency?.agencyName ?? 'Agency Detail'}
          </h2>
          {agency && (
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              {agency.pendingClaimCount} pending claims · ₹{formatRupees(agency.totalPendingAmount)} total
            </p>
          )}
        </div>
      </div>
      <Card padded={false}>
        <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">Pending Claims</span>
        </div>
        {loading ? (
          <div className="py-8 flex justify-center"><Loading /></div>
        ) : claims.length === 0 ? (
          <p className="px-4 py-8 text-sm text-center text-ink-light-muted dark:text-ink-dark-muted">No pending claims found</p>
        ) : (
          <PendingClaimsTable>
            {claims.map((claim, i) => (
              <PendingClaimRow key={claim.claimId} claim={claim} isLast={i === claims.length - 1} />
            ))}
          </PendingClaimsTable>
        )}
      </Card>
    </div>
  )
}
