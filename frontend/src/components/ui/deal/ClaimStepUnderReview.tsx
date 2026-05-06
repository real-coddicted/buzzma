import type { Deal } from '../../../types/DealTypes'
import { ClaimStatusBadge } from './ClaimStatusBadge'
import { IconCheck } from '../icons'

interface ClaimStepUnderReviewProps {
  deal: Deal
}

const COPY: Record<string, { title: string; body: string }> = {
  pending:  {
    title: 'Under Review',
    body:  'Our team is verifying your submitted proof. This usually takes 24–48 hours.',
  },
  approved: {
    title: 'Review Passed',
    body:  'Your submission has been approved. Cashback will be disbursed shortly.',
  },
  rejected: {
    title: 'Submission Rejected',
    body:  'Your proof did not meet the requirements. Please raise a ticket for assistance.',
  },
}

export function ClaimStepUnderReview({ deal }: ClaimStepUnderReviewProps) {
  const status = deal.claimStatus ?? 'pending'
  const copy   = COPY[status]

  return (
    <div className="flex flex-col items-center justify-center gap-4 py-10 text-center">
      <StatusIcon status={status} />

      <div className="space-y-1">
        <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          {copy.title}
        </p>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted max-w-xs leading-relaxed">
          {copy.body}
        </p>
      </div>

      <ClaimStatusBadge status={status} />

      {status === 'rejected' && (
        <button className="mt-2 px-4 py-2 rounded-lg border border-neon-red/40 text-neon-red text-xs font-semibold hover:bg-neon-red/10 transition-colors">
          Raise a Ticket
        </button>
      )}
    </div>
  )
}

function StatusIcon({ status }: { status: string }) {
  if (status === 'approved') {
    return (
      <div className="w-14 h-14 rounded-full bg-neon-green/10 border border-neon-green/30 flex items-center justify-center">
        <IconCheck size={24} className="text-neon-green" />
      </div>
    )
  }

  if (status === 'rejected') {
    return (
      <div className="w-14 h-14 rounded-full bg-neon-red/10 border border-neon-red/30 flex items-center justify-center">
        <span className="text-neon-red text-xl font-bold">✕</span>
      </div>
    )
  }

  return (
    <div className="w-14 h-14 rounded-full bg-neon-yellow/10 border border-neon-yellow/30 flex items-center justify-center">
      <span className="w-6 h-6 rounded-full border-2 border-neon-yellow border-t-transparent animate-spin block" />
    </div>
  )
}
