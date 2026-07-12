interface Props {
  value: string
  onChange: (value: string) => void
}

export function BrandFilter({ value, onChange }: Props) {
  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">Brand</p>
      <input
        type="text"
        placeholder="Nike, Adidas…"
        value={value}
        onChange={e => onChange(e.target.value)}
        className="w-full bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-2 text-xs text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted outline-none focus:border-neon-blue/50 transition-colors"
      />
    </div>
  )
}
