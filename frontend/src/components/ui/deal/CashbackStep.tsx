export function CashbackStep() {
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
        <p className="text-xs font-semibold text-neon-green">Pending Verification</p>
        <p className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
          No further action required. You will be notified once cashback is processed.
        </p>
      </div>
    </div>
  )
}
