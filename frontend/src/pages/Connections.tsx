import { useState, useEffect, useMemo } from 'react'
import { ConnectionsHeader } from '../components/ui/connections/ConnectionsHeader'
import { ConnectionsGrid } from '../components/ui/connections/ConnectionsGrid'
import type { ConnectionStatus, ConnectionFilterOption } from '../components/ui/connections/ConnectionsGrid'
import type { Connection } from '../types/ConnectionTypes'
import { fetchConnections, fetchConnectionSummary } from '../api/connectionApi'
import type { ConnectionSummary } from '../api/connectionApi'

const statusFilters: ConnectionFilterOption[] = [
  { value: 'all',       label: 'All'       },
  { value: 'connected', label: 'Connected' },
  { value: 'pending',   label: 'Pending'   },
  { value: 'invited',   label: 'Invited'   },
]

export function Connections() {
  const [connections, setConnections] = useState<Connection[]>([])
  const [summary, setSummary]         = useState<ConnectionSummary>({ total: 0, connectedCount: 0, pendingCount: 0 })
  const [loading, setLoading]         = useState(true)

  const [search, setSearch]             = useState('')
  const [statusFilter, setStatusFilter] = useState<ConnectionStatus | 'all'>('connected')

  useEffect(() => {
    setLoading(true)
    Promise.all([fetchConnections(), fetchConnectionSummary()]).then(([list, sum]) => {
      setConnections(list)
      setSummary(sum)
      setLoading(false)
    })
  }, [])

  const filtered = useMemo(() => {
    return connections.filter(c => {
      const matchesStatus = statusFilter === 'all' || c.status === statusFilter
      const matchesSearch =
        c.name.toLowerCase().includes(search.toLowerCase()) ||
        c.category.toLowerCase().includes(search.toLowerCase())
      return matchesStatus && matchesSearch
    })
  }, [connections, search, statusFilter])

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <ConnectionsHeader
        total={summary.total}
        connectedCount={summary.connectedCount}
        pendingCount={summary.pendingCount}
        onAddConnection={() => {}}
      />

      {loading ? (
        <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          Loading…
        </div>
      ) : (
        <ConnectionsGrid
          rows={filtered}
          total={summary.total}
          search={search}
          onSearch={setSearch}
          statusFilter={statusFilter}
          onStatusFilter={setStatusFilter}
          filterOptions={statusFilters}
        />
      )}
    </div>
  )
}
