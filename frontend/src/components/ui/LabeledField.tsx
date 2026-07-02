interface LabeledFieldProps {
  label: string
  value: string
  mono?: boolean
}

export function LabeledField({ label, value, mono = false }: LabeledFieldProps) {
  return (
    <div className="flex flex-col gap-3">
      <span className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
        {label}
      </span>
      <span className={[
        'text-sm text-ink-light-primary dark:text-ink-dark-primary',
        mono ? 'font-mono tracking-widest' : '',
      ].join(' ')}>
        {value || '—'}
      </span>
    </div>
  )
}
