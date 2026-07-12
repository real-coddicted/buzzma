interface TabOption<T extends string> {
  value: T
  label: string
}

interface TabsProps<T extends string> {
  options: TabOption<T>[]
  value: T
  onChange: (value: T) => void
}

export function Tabs<T extends string>({ options, value, onChange }: TabsProps<T>) {
  return (
    <div className="flex gap-1 p-1 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border self-start">
      {options.map(o => (
        <button
          key={o.value}
          onClick={() => onChange(o.value)}
          className={[
            'px-3 py-1.5 rounded-md text-xs font-medium transition-all',
            value === o.value
              ? 'bg-surface-light-card dark:bg-surface-dark-card text-ink-light-primary dark:text-ink-dark-primary shadow-sm'
              : 'text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary',
          ].join(' ')}
        >
          {o.label}
        </button>
      ))}
    </div>
  )
}