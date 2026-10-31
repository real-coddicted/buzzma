import { useEffect, useRef, useState } from 'react'
import { Badge } from './Badge'
import { IconEye, IconSearch } from './icons'
import { ROLE_CONFIG } from './users/UserListItem'
import { searchUsers, type UserSummaryDto } from '../../api/userApi'

const SEARCH_DEBOUNCE_MS = 300
const MIN_SEARCH_LENGTH = 2

interface ViewAsPickerProps {
  /** Only users with one of these roles are offered. */
  roles: string[]
  /** Name of the user currently viewed as, if any. */
  selectedName?: string
  onSelect: (user: UserSummaryDto) => void
}

export function ViewAsPicker({ roles, selectedName, onSelect }: ViewAsPickerProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [term, setTerm] = useState('')
  const [users, setUsers] = useState<UserSummaryDto[]>([])
  const [status, setStatus] = useState<'idle' | 'loading' | 'error'>('idle')
  const containerRef = useRef<HTMLDivElement>(null)
  const rolesKey = roles.join(',')

  useEffect(() => {
    const trimmed = term.trim()
    if (trimmed.length < MIN_SEARCH_LENGTH) {
      setUsers([])
      setStatus('idle')
      return
    }
    let cancelled = false
    setStatus('loading')
    const timer = window.setTimeout(() => {
      searchUsers(trimmed, 0, 20, rolesKey.split(','))
        .then(res => {
          if (cancelled) return
          setUsers(res.items ?? [])
          setStatus('idle')
        })
        .catch(err => {
          console.error(err)
          if (!cancelled) setStatus('error')
        })
    }, SEARCH_DEBOUNCE_MS)
    return () => { cancelled = true; window.clearTimeout(timer) }
  }, [term, rolesKey])

  useEffect(() => {
    if (!isOpen) return
    const handleClick = (e: MouseEvent) => {
      if (!containerRef.current?.contains(e.target as Node)) setIsOpen(false)
    }
    const handleKey = (e: KeyboardEvent) => { if (e.key === 'Escape') setIsOpen(false) }
    document.addEventListener('mousedown', handleClick)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('mousedown', handleClick)
      document.removeEventListener('keydown', handleKey)
    }
  }, [isOpen])

  const roleLabels = roles.map(role => ROLE_CONFIG[role]?.label ?? role).join(', ')

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={() => setIsOpen(open => !open)}
        className="flex items-center gap-1.5 h-9 px-2.5 rounded-lg border border-surface-light-border dark:border-surface-dark-border text-xs text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors max-w-[160px]"
        aria-haspopup="listbox"
        aria-expanded={isOpen}
      >
        <IconEye size={14} />
        <span className="truncate hidden sm:inline">{selectedName ? selectedName : 'View as…'}</span>
      </button>

      {isOpen && (
        <div className="absolute right-0 top-11 w-72 max-w-[calc(100vw-1.5rem)] rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-raised dark:bg-surface-dark-raised shadow-lg z-30">
          <div className="flex items-center gap-2 px-3 py-2 border-b border-surface-light-border dark:border-surface-dark-border">
            <IconSearch size={14} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
            <input
              autoFocus
              type="text"
              value={term}
              onChange={e => setTerm(e.target.value)}
              placeholder="Search name or mobile"
              className="bg-transparent text-xs outline-none flex-1 text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
            />
          </div>
          <p className="px-3 pt-2 text-[10px] uppercase tracking-widest text-ink-light-muted dark:text-ink-dark-muted">
            {roleLabels}
          </p>
          <ul role="listbox" className="max-h-72 overflow-y-auto py-1">
            {status === 'loading' && <PickerNote text="Searching…" />}
            {status === 'error' && <PickerNote text="Search failed. Please try again." />}
            {status === 'idle' && term.trim().length < MIN_SEARCH_LENGTH && (
              <PickerNote text={`Type at least ${MIN_SEARCH_LENGTH} characters.`} />
            )}
            {status === 'idle' && term.trim().length >= MIN_SEARCH_LENGTH && users.length === 0 && (
              <PickerNote text="No users found." />
            )}
            {status === 'idle' && users.map(user => {
              const roleConfig = user.role ? ROLE_CONFIG[user.role] : undefined
              return (
                <li key={user.id} role="option" aria-selected={false}>
                  <button
                    type="button"
                    onClick={() => { setIsOpen(false); setTerm(''); onSelect(user) }}
                    className="w-full flex items-center justify-between gap-2 px-3 py-2 text-left hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover"
                  >
                    <span className="min-w-0">
                      <span className="block text-xs font-medium truncate text-ink-light-primary dark:text-ink-dark-primary">{user.name}</span>
                      <span className="block text-[11px] text-ink-light-muted dark:text-ink-dark-muted">{user.mobile}</span>
                    </span>
                    {roleConfig && <Badge variant={roleConfig.variant}>{roleConfig.label}</Badge>}
                  </button>
                </li>
              )
            })}
          </ul>
        </div>
      )}
    </div>
  )
}

function PickerNote({ text }: { text: string }) {
  return <li className="px-3 py-3 text-xs text-ink-light-muted dark:text-ink-dark-muted">{text}</li>
}
