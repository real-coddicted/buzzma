import { useState, useEffect, useCallback, useRef } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { TicketListItem } from '../components/ui/ticket/TicketListItem'
import { TicketDetail } from '../components/ui/ticket/TicketDetail'
import { TicketToolbar } from '../components/ui/ticket/TicketToolbar'
import { TicketTabs } from '../components/ui/ticket/TicketTabs'
import type { TicketTab } from '../components/ui/ticket/TicketTabs'
import { TICKET_STATUS_CONFIG } from '../constants/ticketStatus'
import { fetchMyTickets, fetchAssignedTickets, fetchTicketById } from '../api/ticketApi'
import { RaiseTicketButton } from './RaiseTicket'
import type { Ticket, TicketStatus } from '../types/TicketTypes'
import { useSSE } from '../hooks/useSSE'

interface Props {
  title?: string
  fetchFn?: () => Promise<Ticket[]>
}

export function MyTickets({ title = 'My Tickets', fetchFn = fetchMyTickets }: Props) {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()

  const tab = (searchParams.get('tab') as TicketTab) ?? 'raised'
  const ticketId = searchParams.get('id')
  const isDetailView = searchParams.get('view') === 'detail' && !!ticketId

  const [raisedTickets, setRaisedTickets]     = useState<Ticket[]>([])
  const [assignedTickets, setAssignedTickets] = useState<Ticket[]>([])
  const [raisedLoading, setRaisedLoading]     = useState(true)
  const [assignedLoading, setAssignedLoading] = useState(false)
  const assignedInitialized = useRef(false)
  const [error, setError] = useState<string | null>(null)
  const [filter, setFilter] = useState<TicketStatus | 'all'>('InProgress')
  const [search, setSearch] = useState('')

  const [ticket, setTicket]           = useState<Ticket | null>(null)
  const [ticketLoading, setTicketLoading] = useState(false)

  const loadRaisedTickets = useCallback(() => {
    setRaisedLoading(true)
    fetchFn()
      .then(setRaisedTickets)
      .catch(() => setError('Failed to load tickets. Please try again.'))
      .finally(() => setRaisedLoading(false))
  }, [fetchFn])

  const loadAssignedTickets = useCallback(() => {
    setAssignedLoading(true)
    fetchAssignedTickets()
      .then(setAssignedTickets)
      .catch(() => setError('Failed to load tickets. Please try again.'))
      .finally(() => setAssignedLoading(false))
  }, [])

  useEffect(() => { loadRaisedTickets() }, [loadRaisedTickets])

  useEffect(() => {
    if (tab === 'assigned' && !assignedInitialized.current) {
      assignedInitialized.current = true
      loadAssignedTickets()
    }
  }, [tab, loadAssignedTickets])

  useSSE('EVENT_TYPE_REFRESH', loadRaisedTickets, 'ticket_raised')
  useSSE('EVENT_TYPE_REFRESH', loadAssignedTickets, 'ticket_assigned')

  useEffect(() => {
    if (!ticketId) {
      setTicket(null)
      return
    }
    let cancelled = false
    setTicketLoading(true)
    fetchTicketById(ticketId)
      .then(t => { if (!cancelled) setTicket(t) })
      .catch(() => { if (!cancelled) setError('Failed to load ticket.') })
      .finally(() => { if (!cancelled) setTicketLoading(false) })
    return () => { cancelled = true }
  }, [ticketId])

  function handleSelectTicket(t: Ticket) {
    const params: Record<string, string> = { view: 'detail', id: t.id }
    if (tab === 'assigned') params.tab = 'assigned'
    setSearchParams(params)
  }

  function handleUpdate(updated: Ticket) {
    setTicket(updated)
    if (tab === 'raised') {
      setRaisedTickets(prev => prev.map(t => t.id === updated.id ? updated : t))
    } else {
      setAssignedTickets(prev => prev.map(t => t.id === updated.id ? updated : t))
    }
  }

  const activeTickets = tab === 'raised' ? raisedTickets : assignedTickets
  const loading       = tab === 'raised' ? raisedLoading : assignedLoading

  const filtered = activeTickets.filter(t => {
    const matchesFilter = filter === 'all' || t.status === filter
    const q = search.toLowerCase()
    const matchesSearch = !q
      || t.description.toLowerCase().includes(q)
      || t.categoryDisplayName.toLowerCase().includes(q)
      || t.subCategoryDisplayName.toLowerCase().includes(q)
      || String(t.id).includes(q)
    return matchesFilter && matchesSearch
  })

  if (isDetailView && ticketLoading) {
    return (
      <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted">
        <Loading size={32} />
      </div>
    )
  }

  if (ticket) {
    return (
      <TicketDetail
        ticket={ticket}
        onBack={() => navigate(-1)}
        onUpdate={handleUpdate}
        showActions
        role={tab === 'raised' ? 'reporter' : 'assignee'}
      />
    )
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">{title}</h1>
        </div>
        <RaiseTicketButton size="sm" />
      </div>

      <TicketTabs
        value={tab}
        onChange={t => setSearchParams(t === 'raised' ? {} : { tab: t })}
      />

      <Card padded={false}>
        <TicketToolbar search={search} onSearchChange={setSearch} filter={filter} onFilterChange={setFilter} />

        {loading ? (
          <div className="flex justify-center py-16 text-ink-light-muted dark:text-ink-dark-muted">
            <Loading size={32} />
          </div>
        ) : filtered.length === 0 ? (
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted text-center p-4 py-10">
            {filter === 'all' ? 'No tickets found.' : `No ${TICKET_STATUS_CONFIG[filter as TicketStatus].label} tickets.`}
          </p>
        ) : (
          <div className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {filtered.map(t => (
              <TicketListItem key={t.id} ticket={t} onClick={handleSelectTicket} />
            ))}
          </div>
        )}
      </Card>

      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}