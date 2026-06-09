import { TicketStatusBadge } from './TicketStatusBadge'
import type { Ticket } from '../../../types/TicketTypes'

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

interface TicketListItemProps {
  ticket: Ticket
  onClick: (ticket: Ticket) => void
}

export function TicketListItem({ ticket, onClick }: TicketListItemProps) {
  return (
    <div>
      <button className="w-full text-left group px-4 py-3 hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors" onClick={() => onClick(ticket)}>
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
                {formatDate(ticket.createdAt)}
              </span>
              {ticket.updatedAt !== ticket.createdAt && (
                <span className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
                  · Updated {formatDate(ticket.updatedAt)}
                </span>
              )}
              <span className="text-[11px] font-mono text-ink-light-muted dark:text-ink-dark-muted">
                #{ticket.id}
              </span>
              {(ticket.raisedByName || ticket.raisedBy) && (
                <span className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
                  · By {ticket.raisedByName ?? ticket.raisedBy!.slice(0, 8) + '…'}
                </span>
              )}
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
    </div>
  )
}
