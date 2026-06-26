import { useState, useEffect } from 'react'
import type { LinkedEntity } from '../../../types'
import { fetchConnections } from '../../../api/connectionApi'
import { RupeeInput } from '../RupeeInput'

interface Props {
  entities: LinkedEntity[]
  onChange: (entities: LinkedEntity[]) => void
  openToAll?: boolean
  readOnly?: boolean
}

export function LinkedEntitiesTable({ entities, onChange, openToAll, readOnly }: Props) {
  const [allConnections, setAllConnections] = useState<{ id: string; name: string }[]>([])

  useEffect(() => {
    if (!readOnly) {
      fetchConnections('connected').then(connections => {
        setAllConnections(connections.map(c => ({ id: c.toUserId, name: c.name })))
      }).catch(() => {})
    }
  }, [readOnly])

  const entityMap = new Map(entities.map(e => [e.id, e]))
  const rows = readOnly ? entities.map(e => ({ id: e.id, name: e.name })) : allConnections

  const totalSlots = entities.reduce((sum, e) => sum + e.slotsAvailable, 0)
  const totalCommission = entities.reduce((sum, e) => sum + e.slotsAvailable * e.commissionOffered, 0)

  function handleToggle(conn: { id: string; name: string }) {
    if (entityMap.has(conn.id)) {
      onChange(entities.filter(e => e.id !== conn.id))
    } else {
      onChange([...entities, { id: conn.id, name: conn.name, slotsAvailable: 0, commissionOffered: 0 }])
    }
  }

  function handleSlotsChange(id: string, value: string) {
    const slots = parseInt(value) || 0
    if (entityMap.has(id)) {
      onChange(entities.map(e => e.id === id ? { ...e, slotsAvailable: slots } : e))
    } else {
      const conn = allConnections.find(c => c.id === id)
      if (conn) {
        onChange([...entities, { id, name: conn.name, slotsAvailable: slots, commissionOffered: 0 }])
      }
    }
  }

  function handleCommissionChange(id: string, value: string) {
    const commission = parseFloat(value) || 0
    if (entityMap.has(id)) {
      onChange(entities.map(e => e.id === id ? { ...e, commissionOffered: commission } : e))
    } else {
      const conn = allConnections.find(c => c.id === id)
      if (conn) {
        onChange([...entities, { id, name: conn.name, slotsAvailable: 0, commissionOffered: commission }])
      }
    }
  }

  function copySlotsToAll() {
    const firstEntity = entityMap.get(rows[0]?.id ?? '')
    const slots = firstEntity?.slotsAvailable ?? 0
    const updatedMap = new Map(entities.map(e => [e.id, e]))
    onChange(allConnections.map(c => ({
      ...updatedMap.get(c.id) ?? { id: c.id, name: c.name, commissionOffered: 0 },
      slotsAvailable: slots,
    })))
  }

  function copyCommissionToAll() {
    const firstEntity = entityMap.get(rows[0]?.id ?? '')
    const commission = firstEntity?.commissionOffered ?? 0
    const updatedMap = new Map(entities.map(e => [e.id, e]))
    onChange(allConnections.map(c => ({
      ...updatedMap.get(c.id) ?? { id: c.id, name: c.name, slotsAvailable: 0 },
      commissionOffered: commission,
    })))
  }

  const allSelected = allConnections.length > 0 && allConnections.every(c => entityMap.has(c.id))

  function handleSelectAll() {
    if (allSelected) {
      onChange([])
    } else {
      onChange(allConnections.map(c => entityMap.get(c.id) ?? { id: c.id, name: c.name, slotsAvailable: 0, commissionOffered: 0 }))
    }
  }

  const colSpan = readOnly ? 3 : 4

  return (
    <>
      <div className="overflow-auto max-h-[420px] rounded-lg border border-surface-light-border dark:border-surface-dark-border">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
              {!readOnly && (
                <th className="px-4 py-2.5">
                  <label className="flex items-center gap-1.5 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={!!openToAll || allSelected}
                      disabled={!!openToAll}
                      onChange={handleSelectAll}
                      className="accent-neon-blue cursor-pointer disabled:cursor-not-allowed"
                    />
                    <span className="font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">Select all</span>
                  </label>
                </th>
              )}
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Entity</th>
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Slots</th>
              <th className="text-left px-4 py-2.5 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Commission</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {rows.length === 0 ? (
              <tr>
                <td colSpan={colSpan} className="px-4 py-6 text-center text-ink-light-muted dark:text-ink-dark-muted">
                  No entities available.
                </td>
              </tr>
            ) : (
              rows.map((conn, idx) => {
                const entity = entityMap.get(conn.id)
                const selected = !!openToAll || entityMap.has(conn.id)
                const slotsDisabled = readOnly || !!openToAll
                const commissionDisabled = readOnly
                const isFirst = idx === 0

                return (
                  <tr key={conn.id} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
                    {!readOnly && (
                      <td className="px-4 py-3">
                        <input
                          type="checkbox"
                          checked={selected}
                          disabled={!!openToAll}
                          onChange={() => handleToggle(conn)}
                          className="accent-neon-blue cursor-pointer disabled:cursor-not-allowed"
                        />
                      </td>
                    )}
                    <td className="px-4 py-3 font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                      {conn.name}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1.5">
                        <input
                          type="number"
                          value={entity?.slotsAvailable || ''}
                          onChange={e => handleSlotsChange(conn.id, e.target.value)}
                          disabled={slotsDisabled}
                          onWheel={e => e.currentTarget.blur()}
                          className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg px-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none disabled:opacity-40"
                        />
                        {isFirst && !readOnly && !openToAll && (
                          <button
                            type="button"
                            onClick={copySlotsToAll}
                            title="Copy to all"
                            className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-blue hover:bg-neon-blue/10 px-1.5 py-1 rounded transition-colors whitespace-nowrap"
                          >
                            Copy to all
                          </button>
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1.5">
                        <RupeeInput
                          value={entity?.commissionOffered || ''}
                          onChange={v => handleCommissionChange(conn.id, v)}
                          symbolOffset="left-2"
                          inputPadding="pl-5"
                          disabled={commissionDisabled}
                          className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg pr-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all disabled:opacity-40"
                        />
                        {isFirst && !readOnly && (
                          <button
                            type="button"
                            onClick={copyCommissionToAll}
                            title="Copy to all"
                            className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-blue hover:bg-neon-blue/10 px-1.5 py-1 rounded transition-colors whitespace-nowrap"
                          >
                            Copy to all
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      {rows.length > 0 && (
        <div className="mt-3 p-3 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border grid grid-cols-3 gap-4 text-center">
          <div>
            <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">Selected</p>
            <p className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary mt-0.5">
              {openToAll ? allConnections.length : entities.length}
            </p>
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
