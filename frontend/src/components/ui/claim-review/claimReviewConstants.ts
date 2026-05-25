import type { FilterOption } from '../StatusFilterPills'
import type { ClaimStatus, ReviewStatus, ApprovalMethod } from '../../../types'

export const CLAIM_STATUS_CONFIG: Record<ClaimStatus, { label: string; colorClass: string }> = {
  'in-progress': { label: 'In Progress', colorClass: 'text-neon-blue' },
  'completed':   { label: 'Completed',   colorClass: 'text-neon-green' },
}

export const REVIEW_STATUS_CONFIG: Record<ReviewStatus, { label: string; classes: string; dot: string }> = {
  'pending':   { label: 'Pending',   classes: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/25', dot: 'bg-neon-yellow' },
  'in-review': { label: 'In Review', classes: 'bg-neon-purple/10 text-neon-purple border-neon-purple/25', dot: 'bg-neon-purple animate-pulse-slow' },
  'approved':  { label: 'Approved',  classes: 'bg-neon-green/10  text-neon-green  border-neon-green/25',  dot: 'bg-neon-green' },
  'rejected':  { label: 'Rejected',  classes: 'bg-neon-red/10    text-neon-red    border-neon-red/25',    dot: 'bg-neon-red' },
}

export const APPROVAL_METHOD_CONFIG: Record<ApprovalMethod, { label: string; classes: string }> = {
  manual: { label: 'Manual', classes: 'bg-neon-orange/10 text-neon-orange border-neon-orange/25' },
  auto:   { label: 'Auto',   classes: 'bg-neon-cyan/10   text-neon-cyan   border-neon-cyan/25' },
}

export const REVIEW_FILTER_OPTIONS: FilterOption<ReviewStatus | 'all'>[] = [
  { value: 'all',       label: 'All',       activeClass: 'bg-neon-blue/10   text-neon-blue   border-neon-blue/30'   },
  { value: 'pending',   label: 'Pending',   activeClass: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/30' },
  { value: 'in-review', label: 'In Review', activeClass: 'bg-neon-purple/10 text-neon-purple border-neon-purple/30' },
  { value: 'approved',  label: 'Approved',  activeClass: 'bg-neon-green/10  text-neon-green  border-neon-green/30'  },
  { value: 'rejected',  label: 'Rejected',  activeClass: 'bg-neon-red/10    text-neon-red    border-neon-red/30'    },
]

export const CLAIM_REVIEW_COLUMNS = [
  'Campaign Name',
  'Order ID',
  'Mediator Name',
  'Claim Status',
  'Review Status',
  'Verified',
  'Match %',
  'Actions',
] as const
