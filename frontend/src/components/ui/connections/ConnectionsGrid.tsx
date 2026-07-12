import { useState } from 'react'
import { Card } from '../Card'
import { Loading } from '../Loading'
import { IconUsers, IconCheck, IconX, IconTrash, IconSearch, IconFilter } from '../icons'
import type { Connection, ConnectionStatus, ConnectionSortKey } from '../../../types/ConnectionTypes'

export type { Connection, ConnectionStatus, ConnectionSortKey }

export interface ConnectionFilterOption {
  value: ConnectionStatus | 'all'
  label: string
}

interface ConnectionsGridProps {
  rows: Connection[]
  total: number
  loading?: boolean
  search: string
  onSearch: (value: string) => void
  statusFilter: ConnectionStatus | 'all'
  onStatusFilter: (value: ConnectionStatus | 'all') => void
  filterOptions: ConnectionFilterOption[]
  /** Id of the row whose action is currently in flight — its buttons are disabled. */
  actioningId?: string | null
  onAccept: (connection: Connection) => void
  onReject: (connection: Connection) => void
  onDelete: (connection: Connection) => void
  /** Called when a connected row is clicked to view its details. */
  onRowClick?: (connection: Connection) => void
  /** False when the viewer isn't the approver for pending rows (e.g. viewing parent connections), hiding Accept/Reject. */
  showApprovalActions?: boolean
}

const statusConfig: Record<ConnectionStatus, { label: string; classes: string }> = {
  connected: { label: 'Connected', classes: 'text-neon-green  bg-neon-green/10  border-neon-green/30'  },
  pending:   { label: 'Pending',   classes: 'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/30' },
  rejected:  { label: 'Rejected',  classes: 'text-neon-red    bg-neon-red/10    border-neon-red/30'    },
}

const filterActiveClasses: Record<ConnectionStatus | 'all', string> = {
  all:       'bg-neon-blue/10   text-neon-blue   border-neon-blue/30',
  connected: 'bg-neon-green/10  text-neon-green  border-neon-green/30',
  pending:   'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/30',
  rejected:  'bg-neon-red/10    text-neon-red    border-neon-red/30',
}

const cols: { key: ConnectionSortKey; label: string }[] = [
  { key: 'name',   label: 'Connection' },
  { key: 'status', label: 'Status'     },
]

export function ConnectionsGrid({
  rows,
  total,
  loading = false,
  search,
  onSearch,
  statusFilter,
  onStatusFilter,
  filterOptions,
  actioningId,
  onAccept,
  onReject,
  onDelete,
  onRowClick,
  showApprovalActions = true,
}: ConnectionsGridProps) {
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
      <div className="p-4 flex flex-col sm:flex-row gap-3 border-b border-surface-light-border dark:border-surface-dark-border">
        <div className="flex items-center gap-2 bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-2 flex-1 min-w-0 max-w-xs">
          <IconSearch size={14} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
          <input
            type="text"
            placeholder="Search connections…"
            value={search}
            onChange={e => onSearch(e.target.value)}
            className="bg-transparent text-xs outline-none flex-1 text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
          />
        </div>
        <div className="flex items-center gap-1.5 flex-wrap">
          <IconFilter size={13} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
          {filterOptions.map(f => (
            <button
              key={f.value}
              onClick={() => onStatusFilter(f.value)}
              className={[
                'px-3 py-1 rounded-full text-xs font-medium border transition-all',
                statusFilter === f.value
                  ? filterActiveClasses[f.value]
                  : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
              ].join(' ')}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

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
            {loading ? (
              <tr>
                <td colSpan={4} className="px-5 py-16">
                  <div className="flex flex-col items-center gap-3 text-ink-light-muted dark:text-ink-dark-muted">
                    <Loading size={32} />
                    <span>Loading connections…</span>
                  </div>
                </td>
              </tr>
            ) : sorted.length === 0 ? (
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
                const busy = actioningId === c.id
                const clickable = c.status === 'connected' && !!onRowClick
                return (
                  <tr
                    key={c.id}
                    onClick={clickable ? () => onRowClick(c) : undefined}
                    className={[
                      'hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group',
                      clickable ? 'cursor-pointer' : '',
                    ].join(' ')}
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
                        {c.status === 'pending' && showApprovalActions && (
                          <>
                            <button
                              title="Accept"
                              disabled={busy}
                              onClick={e => { e.stopPropagation(); onAccept(c) }}
                              className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-semibold text-neon-green border border-neon-green/30 bg-neon-green/5 hover:bg-neon-green/15 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                            >
                              <IconCheck size={11} />
                              Accept
                            </button>
                            <button
                              title="Reject"
                              disabled={busy}
                              onClick={e => { e.stopPropagation(); onReject(c) }}
                              className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-semibold text-neon-red border border-neon-red/30 bg-neon-red/5 hover:bg-neon-red/15 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                            >
                              <IconX size={11} />
                              Reject
                            </button>
                          </>
                        )}
                        {c.status === 'connected' && (
                          <button
                            title="Delete"
                            disabled={busy}
                            onClick={e => { e.stopPropagation(); onDelete(c) }}
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-semibold text-neon-red border border-neon-red/30 bg-neon-red/5 hover:bg-neon-red/15 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                          >
                            <IconTrash size={11} />
                            Delete
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
          {loading ? 'Loading connections…' : `Showing ${sorted.length} of ${total} connections`}
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
