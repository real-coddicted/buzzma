import { useState, useEffect } from 'react'
import { Card } from '../components/ui/Card'
import { TicketStatusBadge } from '../components/ui/TicketStatusBadge'
import { TicketDetail } from '../components/ui/TicketDetail'
import { TICKET_STATUS_CONFIG, TICKET_STATUS_FILTERS } from '../constants/ticketStatus'
import { fetchMyTickets } from '../api/ticketApi'
import { RaiseTicketButton } from './RaiseTicket'
import type { Ticket, TicketStatus } from '../types/TicketTypes'

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

export function MyTickets() {
  const [tickets, setTickets] = useState<Ticket[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [filter, setFilter] = useState<TicketStatus | 'all'>('Open')
  const [selected, setSelected] = useState<Ticket | null>(null)

  useEffect(() => {
    fetchMyTickets()
      .then(setTickets)
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

  const filtered = filter === 'all' ? tickets : tickets.filter(t => t.status === filter)

  if (selected) {
    return <TicketDetail ticket={selected} onBack={() => setSelected(null)} />
  }

  return (
    <div className="max-w-4xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">My Tickets</h1>
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {loading ? 'Loading…' : `${tickets.length} ticket${tickets.length !== 1 ? 's' : ''} raised`}
          </p>
        </div>
        <RaiseTicketButton size="sm" />
      </div>

      {/* Filters */}
      <div className="flex items-center gap-1.5 flex-wrap">
        {TICKET_STATUS_FILTERS.map(f => (
          <button
            key={f.value}
            onClick={() => setFilter(f.value)}
            className={[
              'px-3 py-1 rounded-full text-xs font-medium border transition-all',
              filter === f.value
                ? f.activeClasses
                : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
            ].join(' ')}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Content */}
      {error ? (
        <Card>
          <p className="text-sm text-neon-red text-center py-6">Failed to load tickets. Please try again.</p>
        </Card>
      ) : loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-24 rounded-xl bg-surface-light-hover dark:bg-surface-dark-hover animate-pulse" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <Card>
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted text-center py-10">
            {filter === 'all' ? "You haven't raised any tickets yet." : `No ${TICKET_STATUS_CONFIG[filter as TicketStatus].label} tickets.`}
          </p>
        </Card>
      ) : (
        <div className="space-y-3">
          {filtered.map(ticket => (
            <Card key={ticket.id}>
              <button
                className="w-full text-left group"
                onClick={() => setSelected(ticket)}
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0 space-y-1.5">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary group-hover:text-neon-blue transition-colors">
                        {ticket.categoryDisplayName}
                      </span>
                      <span className="text-ink-light-muted dark:text-ink-dark-muted text-xs">›</span>
                      <span className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
                        {ticket.subCategoryDisplayName}
                      </span>
                      {ticket.orderId && (
                        <span className="font-mono text-[11px] text-neon-cyan bg-neon-cyan/10 border border-neon-cyan/25 rounded px-1.5 py-0.5">
                          {ticket.orderId}
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary line-clamp-2">
                      {ticket.description}
                    </p>
                    <div className="flex items-center gap-3 pt-0.5">
                      <span className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
                        Raised {formatDate(ticket.createdAt)}
                      </span>
                      {ticket.updatedAt !== ticket.createdAt && (
                        <span className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
                          · Updated {formatDate(ticket.updatedAt)}
                        </span>
                      )}
                      <span className="text-[11px] font-mono text-ink-light-muted dark:text-ink-dark-muted">
                        #{ticket.id}
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <TicketStatusBadge status={ticket.status} />
                    <svg className="w-3.5 h-3.5 text-ink-light-muted dark:text-ink-dark-muted group-hover:text-neon-blue transition-colors" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <polyline points="9 18 15 12 9 6" />
                    </svg>
                  </div>
                </div>
              </button>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
