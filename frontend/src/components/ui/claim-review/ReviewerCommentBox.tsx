interface ReviewerCommentBoxProps {
  value: string
  onChange: (value: string) => void
  disabled?: boolean
  error?: string
}

export function ReviewerCommentBox({ value, onChange, disabled, error }: ReviewerCommentBoxProps) {
  return (
    <div className="space-y-1">
      <label className="text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary">
        Reviewer Comment <span className="text-neon-red">*</span> (required for rejection)
      </label>
      <textarea
        value={value}
        onChange={e => onChange(e.target.value)}
        disabled={disabled}
        rows={2}
        placeholder="Add your comments here..."
        className="w-full rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-sm text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted px-3 py-2 resize-none focus:outline-none focus:ring-2 focus:ring-neon-blue/50 disabled:opacity-50 disabled:cursor-not-allowed"
      />
      {error && (
        <p className="text-xs text-neon-red">{error}</p>
      )}
    </div>
  )
}
