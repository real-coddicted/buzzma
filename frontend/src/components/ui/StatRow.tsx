interface StatRowProps {
  label: string
  value: string | number
}

export function StatRow({ label, value }: StatRowProps) {
  return (
    <div className="flex items-center justify-between py-3 border-b border-surface-light-border dark:border-surface-dark-border last:border-0">
      <span className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary">
        {label}
      </span>
      <span className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
        {value}
      </span>
    </div>
  )
}
