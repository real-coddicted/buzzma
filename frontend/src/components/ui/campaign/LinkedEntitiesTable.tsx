import type { LinkedEntity } from '../../../types'
import type { ConnectionOption } from '../../../hooks/useConnections'
import { RupeeInput } from '../RupeeInput'

interface Props {
  entities: LinkedEntity[]
  connections?: ConnectionOption[]
  onChange: (entities: LinkedEntity[]) => void
  openToAll?: boolean
  readOnly?: boolean
}

export function LinkedEntitiesTable({ entities, connections = [], onChange, openToAll, readOnly }: Props) {
  const entityMap = new Map(entities.map(e => [e.id, e]))
  const rows = readOnly ? entities.map(e => ({ id: e.id, name: e.name })) : connections

  const totalSlots = entities.reduce((sum, e) => sum + e.slotsAvailable, 0)
  const totalCommission = entities.reduce((sum, e) => sum + e.slotsAvailable * e.commissionOffered, 0)

  function handleToggle(conn: ConnectionOption) {
    if (entityMap.has(conn.id)) {
      onChange(entities.filter(e => e.id !== conn.id))
    } else {
      onChange([...entities, { id: conn.id, name: conn.name, slotsAvailable: 0, commissionOffered: 0 }])
    }
  }

  function upsert(conn: { id: string; name: string }, patch: Partial<LinkedEntity>) {
    if (entityMap.has(conn.id)) {
      onChange(entities.map(e => e.id === conn.id ? { ...e, ...patch } : e))
    } else {
      onChange([...entities, { id: conn.id, name: conn.name, slotsAvailable: 0, commissionOffered: 0, ...patch }])
    }
  }

  function handleSlotsChange(conn: { id: string; name: string }, value: string) {
    upsert(conn, { slotsAvailable: Math.max(0, parseInt(value) || 0) })
  }

  function handleCommissionChange(conn: { id: string; name: string }, value: string) {
    upsert(conn, { commissionOffered: parseFloat(value) || 0 })
  }

  function copySlotsToAll() {
    const slots = entityMap.get(rows[0]?.id ?? '')?.slotsAvailable ?? 0
    onChange(entities.map(e => ({ ...e, slotsAvailable: slots })))
  }

  function copyCommissionToAll() {
    const commission = entityMap.get(rows[0]?.id ?? '')?.commissionOffered ?? 0
    onChange(rows.map(r => ({
      ...(entityMap.get(r.id) ?? { id: r.id, name: r.name, slotsAvailable: 0, commissionOffered: 0 }),
      commissionOffered: commission,
    })))
  }

  const allSelected = connections.length > 0 && connections.every(c => entityMap.has(c.id))

  function handleSelectAll() {
    if (allSelected) {
      onChange([])
    } else {
      onChange(connections.map(c => entityMap.get(c.id) ?? { id: c.id, name: c.name, slotsAvailable: 0, commissionOffered: 0 }))
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
                          min="0"
                          value={entity?.slotsAvailable || ''}
                          onChange={e => handleSlotsChange(conn, e.target.value)}
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
                          onChange={v => handleCommissionChange(conn, v)}
                          symbolOffset="left-2"
                          inputPadding="pl-5"
                          disabled={readOnly}
                          className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg pr-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all disabled:opacity-40"
                        />
                        {isFirst && !readOnly && selected && (
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
              {openToAll ? connections.length : entities.length}
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
