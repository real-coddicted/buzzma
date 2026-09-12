interface CashbackStepProps {
  claimStatus?: string
}

const STATUS_CONFIG: Record<string, { label: string; description: string }> = {
  APPROVED: {
    label: 'Approved',
    description: 'Your claim is approved. Cashback will be credited within 7–14 business days.',
  },
  REWARD_PENDING: {
    label: 'Approved',
    description: 'Your claim is approved. Cashback will be credited within 7–14 business days.',
  },
  COMPLETED: {
    label: 'Cashback Credited',
    description: 'Cashback has been credited to your account.',
  },
}

const DEFAULT_CONFIG = {
  label: 'Pending Verification',
  description: 'No further action required. You will be notified once cashback is processed.',
}

export function CashbackStep({ claimStatus }: CashbackStepProps) {
  const config = (claimStatus && STATUS_CONFIG[claimStatus]) || DEFAULT_CONFIG

  return (
    <div className="space-y-4">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        Your submission is under process. Once approved, cashback will be credited to your account within{' '}
        <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          7–14 business days
        </span>{' '}
        after verification.
      </p>
      <div className="rounded-xl border border-neon-green/20 bg-neon-green/5 px-4 py-4 space-y-1">
        <p className="text-xs font-semibold text-neon-green">{config.label}</p>
        <p className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
          {config.description}
        </p>
      </div>
    </div>
  )
}
