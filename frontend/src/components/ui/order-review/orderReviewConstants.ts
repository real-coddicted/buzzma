import type { FilterOption } from '../StatusFilterPills'
import type { OrderStatus, ReviewStatus, ApprovalMethod } from '../../../types'

export const ORDER_STATUS_CONFIG: Record<OrderStatus, { label: string; classes: string; dot: string }> = {
  pending:    { label: 'Pending',    classes: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/25', dot: 'bg-neon-yellow' },
  processing: { label: 'Processing', classes: 'bg-neon-blue/10   text-neon-blue   border-neon-blue/25',   dot: 'bg-neon-blue animate-pulse-slow' },
  shipped:    { label: 'Shipped',    classes: 'bg-neon-cyan/10   text-neon-cyan   border-neon-cyan/25',   dot: 'bg-neon-cyan' },
  delivered:  { label: 'Delivered',  classes: 'bg-neon-green/10  text-neon-green  border-neon-green/25',  dot: 'bg-neon-green' },
  cancelled:  { label: 'Cancelled',  classes: 'bg-neon-red/10    text-neon-red    border-neon-red/25',    dot: 'bg-neon-red' },
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

export const ORDER_REVIEW_COLUMNS = [
  'Campaign Name',
  'Order ID',
  'Mediator Name',
  'Order Status',
  'Review Status',
  'Approval Method',
  'Verified',
  'Match %',
  'Actions',
] as const
