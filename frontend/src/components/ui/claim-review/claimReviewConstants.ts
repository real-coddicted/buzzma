import type { ClaimStatus, ReviewStatus, ApprovalMethod } from '../../../types'

export const CLAIM_STATUS_CONFIG: Record<ClaimStatus, { label: string; colorClass: string }> = {
  'in-progress': { label: 'In Progress', colorClass: 'text-neon-blue' },
  'completed':   { label: 'Completed',   colorClass: 'text-neon-green' },
}

export const REVIEW_STATUS_CONFIG: Record<ReviewStatus, { label: string; classes: string; activeClass: string }> = {
  'pending':   { label: 'Pending',   classes: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/25', activeClass: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/30' },
  'in-review': { label: 'In Review', classes: 'bg-neon-purple/10 text-neon-purple border-neon-purple/25', activeClass: 'bg-neon-purple/10 text-neon-purple border-neon-purple/30' },
  'objected':  { label: 'Objected',  classes: 'bg-neon-red/10    text-neon-red    border-neon-red/25',    activeClass: 'bg-neon-red/10    text-neon-red    border-neon-red/30'    },
  'approved':  { label: 'Approved',  classes: 'bg-neon-green/10  text-neon-green  border-neon-green/25',  activeClass: 'bg-neon-green/10  text-neon-green  border-neon-green/30'  },
  'rejected':  { label: 'Rejected',  classes: 'bg-neon-red/10    text-neon-red    border-neon-red/25',    activeClass: 'bg-neon-red/10    text-neon-red    border-neon-red/30'    },
}

export const APPROVAL_METHOD_CONFIG: Record<ApprovalMethod, { label: string; classes: string }> = {
  manual: { label: 'Manual', classes: 'bg-neon-orange/10 text-neon-orange border-neon-orange/25' },
  auto:   { label: 'Auto',   classes: 'bg-neon-cyan/10   text-neon-cyan   border-neon-cyan/25' },
}

export const SCREENSHOT_TYPE_CONFIG: Record<string, { label: string; tag: string; tagClass: string }> = {
  SCREENSHOT_TYPE_ORDER:  { label: 'Order Receipt',  tag: 'ORDER',  tagClass: 'bg-neon-blue/10   text-neon-blue   border border-neon-blue/25' },
  SCREENSHOT_TYPE_RATING: { label: 'Product Rating', tag: 'RATING', tagClass: 'bg-neon-yellow/10 text-neon-yellow border border-neon-yellow/25' },
  SCREENSHOT_TYPE_REVIEW: { label: 'Written Review', tag: 'REVIEW', tagClass: 'bg-neon-green/10  text-neon-green  border border-neon-green/25' },
  SCREENSHOT_TYPE_RETURN: { label: 'Return Request', tag: 'RETURN', tagClass: 'bg-neon-red/10    text-neon-red    border border-neon-red/25' },
}

export const CLAIM_REVIEW_COLUMNS = [
  'Campaign Name',
  'Order ID',
  'Platform',
  'Brand Name',
  'Order Date',
  'Mediator Name',
  'Claim Status',
  'Review Status',
  'Mediator Verified',
  'Match %',
  'Actions',
] as const
