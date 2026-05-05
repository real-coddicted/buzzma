import { useState, useRef, useEffect } from 'react'
import type { LinkedEntity } from '../../types'
import { Button } from './Button'
import { IconX, IconSearch } from './icons'

interface SearchEntityModalProps {
  open: boolean
  availableEntities: LinkedEntity[]
  assignedEntityIds: string[]
  onClose: () => void
  onConfirm: (entities: LinkedEntity[]) => void
}

export function SearchEntityModal({
  open,
  availableEntities,
  assignedEntityIds,
  onClose,
  onConfirm,
}: SearchEntityModalProps) {
  const [searchInput, setSearchInput] = useState('')
  const [selectedEntities, setSelectedEntities] = useState<LinkedEntity[]>([])
  const [isDropdownOpen, setIsDropdownOpen] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (open && inputRef.current) {
      inputRef.current.focus()
    }
  }, [open])

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape' && open) {
        handleCancel()
      }
    }

    if (open) {
      document.addEventListener('keydown', handleKeyDown)
      return () => document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open])

  if (!open) return null

  const selectedIds = new Set(selectedEntities.map(e => e.id))

  const filteredEntities = availableEntities.filter(entity => {
    const isAssigned = assignedEntityIds.includes(entity.id)
    const isSelected = selectedIds.has(entity.id)
    const matchesSearch = entity.name.toLowerCase().includes(searchInput.toLowerCase())
    return !isAssigned && !isSelected && matchesSearch
  })

  function handleSelect(entity: LinkedEntity) {
    setSelectedEntities(prev => [...prev, entity])
    setSearchInput('')
    setIsDropdownOpen(false)
  }

  function handleRemove(id: string) {
    setSelectedEntities(prev => prev.filter(e => e.id !== id))
  }

  function handleConfirm() {
    if (selectedEntities.length > 0) {
      onConfirm(selectedEntities)
      setSearchInput('')
      setSelectedEntities([])
    }
  }

  function handleCancel() {
    setSearchInput('')
    setSelectedEntities([])
    setIsDropdownOpen(false)
    onClose()
  }

  return (
    <div className="fixed inset-0 bg-black/50 z-[60] flex items-center justify-center p-4">
      <div className="bg-surface-light-card dark:bg-surface-dark-card rounded-xl border border-surface-light-border dark:border-surface-dark-border w-full max-w-md flex flex-col">
        <div className="flex items-center justify-between p-6 border-b border-surface-light-border dark:border-surface-dark-border">
          <div>
            <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary">
              Search Entity
            </h2>
            <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
              Find and select entities to add
            </p>
          </div>
          <button
            onClick={handleCancel}
            className="p-2 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
          >
            <IconX size={18} />
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div className="relative">
            <div className="flex items-center gap-2 bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-2">
              <IconSearch size={14} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
              <input
                ref={inputRef}
                type="text"
                placeholder="Search entities…"
                value={searchInput}
                onChange={e => {
                  setSearchInput(e.target.value)
                  setIsDropdownOpen(true)
                }}
                onFocus={() => setIsDropdownOpen(true)}
                onBlur={() => setIsDropdownOpen(false)}
                className="bg-transparent text-sm outline-none flex-1 text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
              />
            </div>

            {isDropdownOpen && (
              <div className="absolute top-full left-0 right-0 mt-2 bg-surface-light-card dark:bg-surface-dark-card border border-surface-light-border dark:border-surface-dark-border rounded-lg shadow-lg z-10 max-h-64 overflow-y-auto">
                {filteredEntities.length === 0 ? (
                  <div className="px-4 py-6 text-center text-ink-light-muted dark:text-ink-dark-muted text-sm">
                    {searchInput ? 'No matching entities found.' : 'No available entities.'}
                  </div>
                ) : (
                  <ul className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
                    {filteredEntities.map(entity => (
                      <li key={entity.id}>
                        <button
                          onMouseDown={e => e.preventDefault()}
                          onClick={() => handleSelect(entity)}
                          className="w-full text-left px-4 py-3 hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
                        >
                          <div className="font-semibold text-ink-light-primary dark:text-ink-dark-primary text-sm">
                            {entity.name}
                          </div>
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>

          {selectedEntities.length > 0 && (
            <div className="space-y-1.5">
              <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">
                Selected ({selectedEntities.length})
              </p>
              <ul className="space-y-1.5">
                {selectedEntities.map(entity => (
                  <li
                    key={entity.id}
                    className="flex items-center justify-between gap-2 px-3 py-2 rounded-lg bg-neon-blue/10 border border-neon-blue/30"
                  >
                    <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary text-sm">
                      {entity.name}
                    </span>
                    <button
                      onClick={() => handleRemove(entity.id)}
                      className="p-0.5 rounded text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors flex-shrink-0"
                    >
                      <IconX size={14} />
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <div className="flex items-center justify-end gap-3 p-6 border-t border-surface-light-border dark:border-surface-dark-border">
          <Button variant="secondary" onClick={handleCancel}>
            Cancel
          </Button>
          <Button variant="primary" onClick={handleConfirm} disabled={selectedEntities.length === 0}>
            Add {selectedEntities.length > 1 ? `${selectedEntities.length} Entities` : 'Entity'}
          </Button>
        </div>
      </div>
    </div>
  )
}
