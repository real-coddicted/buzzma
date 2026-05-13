import { useState } from 'react'
import type { LinkedEntity } from '../../../types'
import { IconTrash, IconPlus } from '../icons'
import { SearchEntityModal } from './SearchEntityModal'

interface Props {
  entities: LinkedEntity[]
  availableEntities: LinkedEntity[]
  onChange: (entities: LinkedEntity[]) => void
}

export function LinkedEntitiesTable({ entities, availableEntities, onChange }: Props) {
  const [searchModalOpen, setSearchModalOpen] = useState(false)

  const totalSlots = entities.reduce((sum, e) => sum + e.slotsAvailable, 0)
  const totalCommission = entities.reduce((sum, e) => sum + e.slotsAvailable * e.commissionOffered, 0)

  function handleSlotsChange(id: string, value: string) {
    onChange(entities.map(e => e.id === id ? { ...e, slotsAvailable: parseInt(value) || 0 } : e))
  }

  function handleCommissionChange(id: string, value: string) {
    onChange(entities.map(e => e.id === id ? { ...e, commissionOffered: parseFloat(value) || 0 } : e))
  }

  function handleRemove(id: string) {
    onChange(entities.filter(e => e.id !== id))
  }

  function handleAddFromSearch(incoming: LinkedEntity[]) {
    const newEntities = incoming.filter(e => !entities.some(ex => ex.id === e.id))
    if (newEntities.length > 0) {
      onChange([...entities, ...newEntities.map(e => ({ ...e, slotsAvailable: 0 }))])
    }
    setSearchModalOpen(false)
  }

  return (
    <>
      <div className="flex items-center justify-between mb-3">
        <p className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
          Linked entities assigned to this campaign:
        </p>
        <button
          type="button"
          onClick={() => setSearchModalOpen(true)}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-neon-blue/10 text-neon-blue hover:bg-neon-blue/15 transition-colors text-xs font-semibold"
        >
          <IconPlus size={13} />
          Add
        </button>
      </div>

      <div className="overflow-auto max-h-[420px] rounded-lg border border-surface-light-border dark:border-surface-dark-border">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Entity</th>
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Slots</th>
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Commission</th>
              <th className="px-4 py-2.5" />
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {entities.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-6 text-center text-ink-light-muted dark:text-ink-dark-muted">
                  No entities added yet.
                </td>
              </tr>
            ) : (
              entities.map(entity => (
                <tr key={entity.id} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
                  <td className="px-4 py-3 font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                    {entity.name}
                  </td>
                  <td className="px-4 py-3">
                    <input
                      type="number"
                      value={entity.slotsAvailable}
                      onChange={e => handleSlotsChange(entity.id, e.target.value)}
                      min="0"
                      className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg px-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1">
                      <span className="text-ink-light-muted dark:text-ink-dark-muted">₹</span>
                      <input
                        type="number"
                        value={entity.commissionOffered}
                        onChange={e => handleCommissionChange(entity.id, e.target.value)}
                        step="1"
                        min="0"
                        className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg px-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all"
                      />
                    </div>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() => handleRemove(entity.id)}
                      className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-red hover:bg-neon-red/10 transition-colors"
                    >
                      <IconTrash size={14} />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {entities.length > 0 && (
        <div className="mt-3 p-3 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border grid grid-cols-3 gap-4 text-center">
          <div>
            <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">Entities</p>
            <p className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary mt-0.5">{entities.length}</p>
          </div>
          <div>
            <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">Total Slots</p>
            <p className="text-sm font-bold text-neon-cyan mt-0.5">{totalSlots}</p>
          </div>
          <div>
            <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">Commission</p>
            <p className="text-sm font-bold text-neon-green mt-0.5">₹{totalCommission.toLocaleString()}</p>
          </div>
        </div>
      )}

      <SearchEntityModal
        open={searchModalOpen}
        availableEntities={availableEntities}
        assignedEntityIds={entities.map(e => e.id)}
        onClose={() => setSearchModalOpen(false)}
        onConfirm={handleAddFromSearch}
      />
    </>
  )
}
