import { useState, useEffect, useMemo } from 'react'
import { ConnectionsHeader } from '../components/ui/connections/ConnectionsHeader'
import { SearchInput } from '../components/ui/SearchInput'
import { StatusFilterPills } from '../components/ui/StatusFilterPills'
import type { FilterOption } from '../components/ui/StatusFilterPills'
import { ConnectionsGrid } from '../components/ui/connections/ConnectionsGrid'
import type { ConnectionStatus } from '../components/ui/connections/ConnectionsGrid'
import type { Connection } from '../types/ConnectionTypes'
import { fetchConnections, fetchConnectionSummary } from '../api/connectionApi'
import type { ConnectionSummary } from '../api/connectionApi'

const statusFilters: FilterOption<ConnectionStatus | 'all'>[] = [
  { value: 'all',       label: 'All',       activeClasses: 'bg-neon-blue/10   text-neon-blue   border-neon-blue/30'   },
  { value: 'connected', label: 'Connected', activeClasses: 'bg-neon-green/10  text-neon-green  border-neon-green/30'  },
  { value: 'pending',   label: 'Pending',   activeClasses: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/30' },
  { value: 'invited',   label: 'Invited',   activeClasses: 'bg-neon-orange/10 text-neon-orange border-neon-orange/30' },
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

      <div className="flex flex-col sm:flex-row gap-3">
        <SearchInput
          value={search}
          onChange={setSearch}
          placeholder="Search connections…"
        />
        <StatusFilterPills
          options={statusFilters}
          value={statusFilter}
          onChange={setStatusFilter}
        />
      </div>

      {loading ? (
        <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          Loading…
        </div>
      ) : (
        <ConnectionsGrid rows={filtered} total={summary.total} />
      )}
    </div>
  )
}
