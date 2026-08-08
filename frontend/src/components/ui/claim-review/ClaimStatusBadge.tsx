import { CLAIM_STATUS_CONFIG, APPROVAL_METHOD_CONFIG } from './claimReviewConstants'
import type { ClaimStatus, ApprovalMethod } from '../../../types'

interface ClaimStatusBadgeProps {
  status: ClaimStatus
  approvalMethod?: ApprovalMethod
}

export function ClaimStatusBadge({ status, approvalMethod }: ClaimStatusBadgeProps) {
  const { label, colorClass } = CLAIM_STATUS_CONFIG[status]
  return (
    <div>
      {status === 'APPROVED' && approvalMethod && (
        <div className="text-[10px] font-medium text-neon-orange mb-0.5">
          {APPROVAL_METHOD_CONFIG[approvalMethod].label}
        </div>
      )}
      <span className={`${colorClass} text-center`}>{label}</span>
    </div>
  )
}
