import { REVIEW_STATUS_CONFIG, APPROVAL_METHOD_CONFIG } from './claimReviewConstants'
import type { ReviewStatus, ApprovalMethod } from '../../../types'

interface ReviewStatusCellProps {
  status: ReviewStatus
  approvalMethod: ApprovalMethod
}

export function ReviewStatusCell({ status, approvalMethod }: ReviewStatusCellProps) {
  const { label, classes } = REVIEW_STATUS_CONFIG[status]
  return (
    <div>
      {status === 'approved' && (
        <div className="text-[10px] font-medium text-neon-orange mb-0.5">
          {APPROVAL_METHOD_CONFIG[approvalMethod].label}
        </div>
      )}
      <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', classes].join(' ')}>
        {label}
      </span>
    </div>
  )
}
