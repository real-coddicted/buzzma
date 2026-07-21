import { useMemo, useState } from 'react'
import { IconCheck, IconX } from '../../icons'

export interface TypeaheadOption {
  value: string
  label: string
}

interface Props {
  label: string
  options: TypeaheadOption[]
  selected: Set<string>
  onChange: (selected: Set<string>) => void
  placeholder?: string
}

export function TypeaheadMultiSelect({ label, options, selected, onChange, placeholder }: Props) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return options
    return options.filter(o => o.label.toLowerCase().includes(q))
  }, [options, query])

  const selectedOptions = useMemo(() => options.filter(o => selected.has(o.value)), [options, selected])

  function toggle(value: string) {
    const next = new Set(selected)
    if (next.has(value)) next.delete(value); else next.add(value)
    onChange(next)
  }

  return (
    <div>
      <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-2">{label}</p>
      <div className="relative">
        <input
          type="text"
          value={query}
          onChange={e => { setQuery(e.target.value); setOpen(true) }}
          onFocus={() => setOpen(true)}
          onBlur={() => setTimeout(() => setOpen(false), 150)}
          placeholder={placeholder ?? `Search ${label.toLowerCase()}…`}
          className="w-full bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-2 text-xs text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted outline-none focus:border-neon-blue/50 transition-colors"
        />
        {open && filtered.length > 0 && (
          <div className="absolute left-0 right-0 z-10 mt-1 max-h-40 overflow-y-auto rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-lg">
            {filtered.map(opt => (
              <button
                key={opt.value}
                type="button"
                onMouseDown={e => e.preventDefault()}
                onClick={() => toggle(opt.value)}
                className="w-full flex items-center justify-between gap-2 px-3 py-1.5 text-xs text-left text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover"
              >
                <span>{opt.label}</span>
                {selected.has(opt.value) && <IconCheck size={12} className="text-neon-blue flex-shrink-0" />}
              </button>
            ))}
          </div>
        )}
      </div>
      {selectedOptions.length > 0 && (
        <div className="flex flex-wrap gap-1.5 mt-2">
          {selectedOptions.map(opt => (
            <span
              key={opt.value}
              className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium border bg-neon-blue/10 border-neon-blue/20 text-neon-blue"
            >
              {opt.label}
              <button onClick={() => toggle(opt.value)} className="hover:text-neon-red transition-colors">
                <IconX size={10} />
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}
