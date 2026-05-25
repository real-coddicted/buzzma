import { IconOrderInProgress, IconOrderCompleted } from '../icons'
import { CLAIM_STATUS_CONFIG } from './claimReviewConstants'
import type { ClaimStatus } from '../../../types'

export function ClaimStatusBadge({ status }: { status: ClaimStatus }) {
  const { label, colorClass } = CLAIM_STATUS_CONFIG[status]
  return (
    <span className={colorClass} title={label}>
      {status === 'in-progress' ? <IconOrderInProgress /> : <IconOrderCompleted />}
    </span>
  )
}
