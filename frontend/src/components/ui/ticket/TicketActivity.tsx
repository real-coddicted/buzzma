import { useState, useEffect } from 'react'
import { fetchTicketActivity } from '../../../api/ticketApi'
import type { Ticket, TicketActivityEvent } from '../../../types/TicketTypes'

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}


const actorColor: Record<TicketActivityEvent['actorRole'], string> = {
  shopper: 'text-neon-orange',
  support: 'text-neon-purple',
  system:  'text-neon-purple',
}

const actorDot: Record<TicketActivityEvent['actorRole'], string> = {
  shopper: 'bg-neon-orange',
  support: 'bg-neon-purple',
  system:  'bg-neon-purple',
}

interface Props {
  ticket: Ticket
}

export function TicketActivity({ ticket }: Props) {
  const [events, setEvents] = useState<TicketActivityEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    setLoading(true)
    setError(false)
    fetchTicketActivity(ticket)
      .then(setEvents)
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [ticket.id])

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card flex flex-col overflow-hidden">
      <div className="px-5 py-4 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
        <h2 className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Activity
          {!loading && !error && (
            <span className="ml-2 text-[11px] font-normal text-ink-light-muted dark:text-ink-dark-muted">
              {events.length}
            </span>
          )}
        </h2>
      </div>

      <div className="flex-1 overflow-y-auto px-5 py-4">
        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="flex gap-3">
                <div className="w-2 h-2 mt-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover animate-pulse flex-shrink-0" />
                <div className="flex-1 space-y-1.5">
                  <div className="h-2.5 rounded bg-surface-light-hover dark:bg-surface-dark-hover animate-pulse w-3/4" />
                  <div className="h-2 rounded bg-surface-light-hover dark:bg-surface-dark-hover animate-pulse w-1/2" />
                </div>
              </div>
            ))}
          </div>
        ) : error ? (
          <p className="text-xs text-neon-red">Failed to load activity.</p>
        ) : events.length === 0 ? (
          <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted py-4 text-center">
            No activity yet.
          </p>
        ) : (
          <ol className="relative">
            {events.map((ev, idx) => {
              const isLast = idx === events.length - 1
              return (
                <li key={ev.id} className="flex gap-3 relative">
                  {/* Timeline line */}
                  {!isLast && (
                    <span className="absolute left-[3px] top-4 bottom-0 w-px bg-surface-light-border dark:bg-surface-dark-border" />
                  )}
                  {/* Dot */}
                  <span className={['w-2 h-2 rounded-full mt-1.5 flex-shrink-0 relative z-10', actorDot[ev.actorRole]].join(' ')} />
                  {/* Content */}
                  <div className={['min-w-0', isLast ? 'pb-0' : 'pb-4'].join(' ')}>
                    <p className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary leading-snug">
                      {ev.description}
                    </p>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      <span className={['text-[10px] font-medium', actorColor[ev.actorRole]].join(' ')}>
                        {ev.actor}
                      </span>
                      <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                        · {formatDateTime(ev.createdAt)}
                      </span>
                    </div>
                  </div>
                </li>
              )
            })}
          </ol>
        )}
      </div>
    </div>
  )
}
