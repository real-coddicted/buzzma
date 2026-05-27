interface ClaimProofScoreBarProps {
  score: number
}

export function ClaimProofScoreBar({ score }: ClaimProofScoreBarProps) {
  const color =
    score >= 80 ? 'bg-neon-green' : score >= 50 ? 'bg-neon-yellow' : 'bg-neon-red'
  const text =
    score >= 80 ? 'text-neon-green' : score >= 50 ? 'text-neon-yellow' : 'text-neon-red'

  return (
    <div className="flex flex-col gap-1.5 pt-3 border-t border-surface-light-border dark:border-surface-dark-border">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          Overall Score
        </span>
        <span className={['text-xs font-semibold tabular-nums', text].join(' ')}>
          {score}%
        </span>
      </div>
      <div className="h-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
        <div
          className={['h-full rounded-full transition-all', color].join(' ')}
          style={{ width: `${score}%` }}
        />
      </div>
    </div>
  )
}
