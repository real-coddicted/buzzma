import type { ClaimReviewItem } from '../../../types'
import { ClaimStatusBadge } from './ClaimStatusBadge'
import { ReviewStatusCell } from './ReviewStatusCell'

function paise(amount: number) {
  return `₹${(amount / 100).toLocaleString('en-IN')}`
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex justify-between items-center gap-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border last:border-0">
      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">{label}</span>
      <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary text-right truncate">{children}</span>
    </div>
  )
}

interface ClaimInfoProps {
  claim: ClaimReviewItem
}

export function ClaimInfo({ claim }: ClaimInfoProps) {
  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-hidden flex flex-col">
      <div className="px-5 pt-5 pb-3 flex-shrink-0">
        <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary leading-snug">
          Claim Info
        </h2>
      </div>

      <div className="px-5 pb-5 overflow-y-auto">
        <Row label="Campaign">{claim.campaignName}</Row>
        {claim.productName && <Row label="Product">{claim.productName}</Row>}
        {claim.sellerName && <Row label="Seller">{claim.sellerName}</Row>}
        <Row label="Order ID"><span className="font-mono">{claim.orderId}</span></Row>
        <Row label="Order Date"><span className="font-mono">{claim.orderDate}</span></Row>
        {claim.amountPaise != null && <Row label="Amount">{paise(claim.amountPaise)}</Row>}
        {claim.accountName && <Row label="Account Name">{claim.accountName}</Row>}
        {claim.mediatorName && <Row label="Mediator">{claim.mediatorName}</Row>}
        <Row label="Mediator Verified">
          {claim.mediatorVerified
            ? <span className="text-neon-green">Yes</span>
            : <span className="text-ink-light-muted dark:text-ink-dark-muted">No</span>}
        </Row>
        <Row label="Claim Status"><ClaimStatusBadge status={claim.claimStatus} /></Row>
        <Row label="Review Status">
          <ReviewStatusCell status={claim.reviewStatus} approvalMethod={claim.approvalMethod} />
        </Row>
        {claim.currentStep != null && <Row label="Current Step">{claim.currentStep}</Row>}
        <Row label="Match Score">
          <span className={
            claim.matchPct >= 80 ? 'text-neon-green' :
            claim.matchPct >= 50 ? 'text-neon-yellow' :
            'text-neon-red'
          }>{claim.matchPct}%</span>
        </Row>
        {claim.reviewUrl && (
          <Row label="Review URL">
            <a
              href={claim.reviewUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-neon-cyan hover:text-neon-blue transition-colors truncate"
            >
              {claim.reviewUrl}
            </a>
          </Row>
        )}
        {claim.reviewerComments && <Row label="Reviewer Comments">{claim.reviewerComments}</Row>}
      </div>
    </div>
  )
}
