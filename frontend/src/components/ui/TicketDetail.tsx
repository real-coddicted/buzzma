import { TicketInfo } from './TicketInfo'
import { TicketComments } from './TicketComments'
import type { Ticket } from '../../types/TicketTypes'

interface Props {
  ticket: Ticket
  onBack: () => void
}

export function TicketDetail({ ticket, onBack }: Props) {
  return (
    <div className="max-w-6xl mx-auto space-y-4">
      {/* Back + header */}
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
          aria-label="Back"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="15 18 9 12 15 6" />
          </svg>
        </button>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Ticket <span className="font-mono text-ink-light-muted dark:text-ink-dark-muted text-base">#{ticket.id}</span>
        </h1>
      </div>

      {/* Two-column layout — fills remaining viewport height */}
      {/* 64px topbar + ~56px page header + 24px padding + 16px gap = ~160px offset */}
      <div
        className="grid grid-cols-1 lg:grid-cols-2 gap-4"
        style={{ height: 'calc(100vh - 160px)' }}
      >
        <TicketInfo ticket={ticket} />
        <TicketComments ticket={ticket} />
      </div>
    </div>
  )
}
