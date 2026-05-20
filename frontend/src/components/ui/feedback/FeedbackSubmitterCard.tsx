interface FeedbackSubmitterCardProps {
  name: string
  email: string
}

export function FeedbackSubmitterCard({ name, email }: FeedbackSubmitterCardProps) {
  const initial = name.charAt(0).toUpperCase()

  return (
    <div className="flex items-center gap-3 p-3 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border">
      <div
        className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold text-white flex-shrink-0"
        style={{ background: 'linear-gradient(135deg, #ff79c6 0%, #bd93f9 100%)' }}
      >
        {initial}
      </div>
      <div className="min-w-0">
        <div className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          {name}
        </div>
        <div className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
          {`Submitting as ${email}`}
        </div>
      </div>
    </div>
  )
}

