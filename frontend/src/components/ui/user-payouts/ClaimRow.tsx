import { PlatformBadge } from '../campaign/CampaignBadges'
import { CopyableCode } from '../CopyableCode'
import { formatRupees } from '../../../utils/currency'
import type { PayoutClaim } from '../../../types/UserPayoutsTypes'

interface ClaimRowProps {
  claim: PayoutClaim
  isLast: boolean
}

export function ClaimRow({ claim, isLast }: ClaimRowProps) {
  return (
    <tr className={!isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : ''}>
      <td className="px-4 py-3">
        <div className="flex flex-col items-start gap-1">
          <CopyableCode code={claim.claimCode} />
          <span className="text-xs font-mono text-ink-light-secondary dark:text-ink-dark-secondary">{claim.ecommerceOrderId}</span>
        </div>
      </td>
      <td className="px-4 py-3">
        <div className="flex items-center gap-2">
          <span className="text-sm text-ink-light-primary dark:text-ink-dark-primary">{claim.campaign}</span>
          {claim.platform && <PlatformBadge platform={claim.platform} />}
        </div>
      </td>
      <td className="px-4 py-3 text-sm text-ink-light-secondary dark:text-ink-dark-secondary">{claim.brand}</td>
      <td className="px-4 py-3 text-xs text-ink-light-muted dark:text-ink-dark-muted">{claim.approvedDate}</td>
      <td className="px-4 py-3 text-sm font-semibold text-right text-neon-green">₹{formatRupees(claim.amount)}</td>
    </tr>
  )
}
