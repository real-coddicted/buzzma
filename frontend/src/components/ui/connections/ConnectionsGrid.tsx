import { useState } from 'react'
import { Card } from '../Card'
import { IconUsers, IconCheck } from '../icons'
import type { Connection, ConnectionStatus, ConnectionSortKey } from '../../../types/ConnectionTypes'

export type { Connection, ConnectionStatus, ConnectionSortKey }

interface ConnectionsGridProps {
  rows: Connection[]
  total: number
}

const statusConfig: Record<ConnectionStatus, { label: string; classes: string }> = {
  connected: { label: 'Connected', classes: 'text-neon-green  bg-neon-green/10  border-neon-green/30'  },
  pending:   { label: 'Pending',   classes: 'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/30' },
  invited:   { label: 'Invited',   classes: 'text-neon-blue   bg-neon-blue/10   border-neon-blue/30'   },
}

const cols: { key: ConnectionSortKey; label: string }[] = [
  { key: 'name',   label: 'Connection' },
  { key: 'status', label: 'Status'     },
]

export function ConnectionsGrid({ rows, total }: ConnectionsGridProps) {
  const [sortBy, setSortBy]   = useState<ConnectionSortKey>('name')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc')

  function handleSort(key: ConnectionSortKey) {
    if (sortBy === key) {
      setSortDir(d => (d === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortBy(key)
      setSortDir('asc')
    }
  }

  const sorted = [...rows].sort((a, b) => {
    const av = a[sortBy] ?? ''
    const bv = b[sortBy] ?? ''
    const cmp = String(av).localeCompare(String(bv))
    return sortDir === 'asc' ? cmp : -cmp
  })

  function SortIndicator({ col }: { col: ConnectionSortKey }) {
    if (sortBy !== col) return <span className="text-ink-light-muted dark:text-ink-dark-muted opacity-40">↕</span>
    return <span className="text-neon-blue">{sortDir === 'asc' ? '↑' : '↓'}</span>
  }

  return (
    <Card padded={false}>
      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
              {cols.map(col => (
                <th
                  key={col.key}
                  onClick={() => handleSort(col.key)}
                  className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted select-none cursor-pointer hover:text-ink-light-primary dark:hover:text-ink-dark-primary"
                >
                  <span className="inline-flex items-center gap-1">
                    {col.label}
                    <SortIndicator col={col.key} />
                  </span>
                </th>
              ))}
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                Since
              </th>
              <th className="px-5 py-3 text-right font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {sorted.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                  <div className="flex flex-col items-center gap-2">
                    <IconUsers size={24} />
                    <span>No connections match your filter.</span>
                  </div>
                </td>
              </tr>
            ) : (
              sorted.map(c => {
                const s = statusConfig[c.status]
                return (
                  <tr
                    key={c.id}
                    className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group"
                  >
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <div
                          className="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold text-surface-dark-base flex-shrink-0"
                          style={{ background: c.avatarColor }}
                        >
                          {c.avatar}
                        </div>
                        <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                          {c.name}
                        </span>
                      </div>
                    </td>

                    <td className="px-5 py-4">
                      <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', s.classes].join(' ')}>
                        {s.label}
                      </span>
                    </td>

                    <td className="px-5 py-4 text-ink-light-muted dark:text-ink-dark-muted font-mono">
                      {c.since ?? '—'}
                    </td>

                    <td className="px-5 py-4">
                      <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        {c.status === 'pending' && (
                          <button
                            title="Accept"
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-semibold text-neon-green border border-neon-green/30 bg-neon-green/5 hover:bg-neon-green/15 transition-colors"
                          >
                            <IconCheck size={11} />
                            Accept
                          </button>
                        )}
                        {c.status === 'invited' && (
                          <button
                            title="Withdraw"
                            className="px-2.5 py-1 rounded-lg text-[10px] font-semibold text-ink-light-muted dark:text-ink-dark-muted border border-surface-light-border dark:border-surface-dark-border hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
                          >
                            Withdraw
                          </button>
                        )}
                        {c.status === 'connected' && (
                          <button
                            title="Remove"
                            className="px-2.5 py-1 rounded-lg text-[10px] font-semibold text-neon-red border border-neon-red/30 bg-neon-red/5 hover:bg-neon-red/10 transition-colors"
                          >
                            Remove
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

      {/* Footer */}
      <div className="px-5 py-3 border-t border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
        <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
          Showing {sorted.length} of {total} connections
        </span>
        <div className="flex items-center gap-1">
          <button className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40" disabled>
            Previous
          </button>
          <button className="px-3 py-1.5 text-xs rounded-lg bg-neon-blue/10 border border-neon-blue/30 text-neon-blue font-semibold">
            1
          </button>
          <button className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40" disabled>
            Next
          </button>
        </div>
      </div>
    </Card>
  )
}
