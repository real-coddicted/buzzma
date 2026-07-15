import type { ClaimReviewItem } from '../../../types'
import { ClaimStatusBadge } from './ClaimStatusBadge'
import { ReviewStatusCell } from './ReviewStatusCell'
import { IconInfo } from '../icons'

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex justify-between items-start gap-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border last:border-0">
      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">{label}</span>
      <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary text-right break-words min-w-0">{children}</span>
    </div>
  )
}

interface ClaimInfoProps {
  claim: ClaimReviewItem
  campaignTitle?: string
}

export function ClaimInfo({ claim, campaignTitle }: ClaimInfoProps) {
  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-y-auto flex flex-col">
      <div className="px-5 pt-5 pb-3">
        <div className="flex items-center gap-2">
          <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary leading-snug">
            Claim Info
          </h2>
          <div className="relative group flex-shrink-0">
            <IconInfo size={15} className="text-ink-light-muted dark:text-ink-dark-muted cursor-help" />
          </div>
        </div>
      </div>

      <div className="px-5 pb-5">
        <Row label="Campaign Title">{campaignTitle ?? claim.campaignName}</Row>
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
        <Row label="Match Score">
          <span className={
            claim.matchPct >= 80 ? 'text-neon-green' :
            claim.matchPct >= 50 ? 'text-neon-yellow' :
            'text-neon-red'
          }>{claim.matchPct}%</span>
        </Row>
        {claim.reviewerComments && <Row label="Reviewer Comments">{claim.reviewerComments}</Row>}
      </div>
    </div>
  )
}
