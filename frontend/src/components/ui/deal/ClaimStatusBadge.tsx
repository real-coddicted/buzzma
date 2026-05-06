import { CLAIM_STATUS_CONFIG } from '../../../constants/claim'
import type { ClaimStatus } from '../../../types/DealTypes'

interface ClaimStatusBadgeProps {
  status: ClaimStatus
}

export function ClaimStatusBadge({ status }: ClaimStatusBadgeProps) {
  const { label, classes } = CLAIM_STATUS_CONFIG[status]
  return (
    <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', classes].join(' ')}>
      {label}
    </span>
  )
}
