import { useState, useRef } from 'react'
import { IconSearch, IconPlus } from '../icons'

export interface ConnectionOption {
  id: string
  name: string
}

interface SearchConnectionsBarProps {
  options: ConnectionOption[]
  onAdd: (option: ConnectionOption) => void
  placeholder?: string
  addLabel?: string
}

export function SearchConnectionsBar({
  options,
  onAdd,
  placeholder = 'Search connections…',
  addLabel = 'Add',
}: SearchConnectionsBarProps) {
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState<ConnectionOption | null>(null)
  const [isOpen, setIsOpen] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const filtered = search
    ? options.filter(o => o.name.toLowerCase().includes(search.toLowerCase()))
    : options

  function handleInputChange(value: string) {
    setSearch(value)
    setSelected(null)
    setIsOpen(true)
  }

  function handleSelect(option: ConnectionOption) {
    setSelected(option)
    setSearch(option.name)
    setIsOpen(false)
  }

  function handleAdd() {
    if (!selected) return
    onAdd(selected)
    setSearch('')
    setSelected(null)
    inputRef.current?.focus()
  }

  return (
    <div className="flex items-center gap-3">
      <div className="relative flex-1 min-w-0">
        <div className="flex items-center gap-2 bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-2">
          <IconSearch size={14} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
          <input
            ref={inputRef}
            type="text"
            placeholder={placeholder}
            value={search}
            onChange={e => handleInputChange(e.target.value)}
            onFocus={() => setIsOpen(true)}
            onBlur={() => setIsOpen(false)}
            className="bg-transparent text-xs outline-none flex-1 text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
          />
        </div>

        {isOpen && (
          <ul className="absolute z-20 top-full left-0 right-0 mt-1 bg-surface-light-card dark:bg-surface-dark-card border border-surface-light-border dark:border-surface-dark-border rounded-lg shadow-lg max-h-52 overflow-y-auto">
            {filtered.length === 0 ? (
              <li className="px-4 py-3 text-xs text-ink-light-muted dark:text-ink-dark-muted text-center">
                {search ? 'No matches found.' : 'No connections available.'}
              </li>
            ) : (
              filtered.map(option => (
                <li key={option.id}>
                  <button
                    type="button"
                    onMouseDown={e => e.preventDefault()}
                    onClick={() => handleSelect(option)}
                    className="w-full text-left px-4 py-2.5 text-xs text-ink-light-primary dark:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors font-medium"
                  >
                    {option.name}
                  </button>
                </li>
              ))
            )}
          </ul>
        )}
      </div>

      <button
        type="button"
        onClick={handleAdd}
        disabled={!selected}
        className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-neon-blue text-surface-dark-base hover:brightness-110 transition-all shadow-neon-blue/30 disabled:opacity-50 disabled:cursor-not-allowed flex-shrink-0"
      >
        <IconPlus size={13} />
        {addLabel}
      </button>
    </div>
  )
}
