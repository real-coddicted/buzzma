import { useState, useEffect } from 'react'
import type { LinkedEntity } from '../../../types'
import { IconTrash } from '../icons'
import { SearchConnectionsBar } from '../connections/SearchConnectionsBar'
import type { ConnectionOption } from '../connections/SearchConnectionsBar'
import { fetchConnections } from '../../../api/connectionApi'
import { RupeeInput } from '../RupeeInput'

interface Props {
  entities: LinkedEntity[]
  onChange: (entities: LinkedEntity[]) => void
}

export function LinkedEntitiesTable({ entities, onChange }: Props) {
  const [availableOptions, setAvailableOptions] = useState<ConnectionOption[]>([])

  useEffect(() => {
    fetchConnections('connected').then(connections => {
      setAvailableOptions(connections.map(c => ({ id: c.toUserId, name: c.name })))
    }).catch(() => {})
  }, [])

  const totalSlots = entities.reduce((sum, e) => sum + e.slotsAvailable, 0)
  const totalCommission = entities.reduce((sum, e) => sum + e.slotsAvailable * e.commissionOffered, 0)

  const assignedIds = new Set(entities.map(e => e.id))
  const unassignedOptions = availableOptions.filter(o => !assignedIds.has(o.id))

  function handleAdd(option: ConnectionOption) {
    onChange([...entities, { id: option.id, name: option.name, slotsAvailable: 0, commissionOffered: 0 }])
  }

  function handleSlotsChange(id: string, value: string) {
    onChange(entities.map(e => e.id === id ? { ...e, slotsAvailable: parseInt(value) || 0 } : e))
  }

  function handleCommissionChange(id: string, value: string) {
    onChange(entities.map(e => e.id === id ? { ...e, commissionOffered: parseFloat(value) || 0 } : e))
  }

  function handleRemove(id: string) {
    onChange(entities.filter(e => e.id !== id))
  }

  return (
    <>
      <div className="mb-3">
        <SearchConnectionsBar
          options={unassignedOptions}
          onAdd={handleAdd}
          placeholder="Search linked entities…"
        />
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
                      value={entity.slotsAvailable || ''}
                      onChange={e => handleSlotsChange(entity.id, e.target.value)}
                      className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg px-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <RupeeInput
                      value={entity.commissionOffered || ''}
                      onChange={v => handleCommissionChange(entity.id, v)}
                      symbolOffset="left-2"
                      inputPadding="pl-5"
                      className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg pr-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all"
                    />
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
    </>
  )
}
