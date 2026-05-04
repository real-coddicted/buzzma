import { TicketStatusBadge } from './TicketStatusBadge'
import type { Ticket } from '../../types/TicketTypes'

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
  })
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex items-center gap-2">
      <dt className="text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted w-24 flex-shrink-0">
        {label}
      </dt>
      <dd className="flex items-center flex-wrap gap-1">{children}</dd>
    </div>
  )
}

interface Props {
  ticket: Ticket
}

export function TicketInfo({ ticket }: Props) {
  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-y-auto p-5 space-y-4">
      <div className="flex items-start justify-between gap-2">
        <h2 className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Ticket Details
        </h2>
        <TicketStatusBadge status={ticket.status} />
      </div>

      <dl className="space-y-3">
        <Row label="Category">
          <span className="text-xs text-ink-light-primary dark:text-ink-dark-primary font-medium">
            {ticket.categoryDisplayName}
          </span>
          <span className="text-ink-light-muted dark:text-ink-dark-muted mx-1.5">›</span>
          <span className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
            {ticket.subCategoryDisplayName}
          </span>
        </Row>

        {ticket.orderId && (
          <Row label="Order ID">
            <span className="font-mono text-[11px] text-neon-cyan bg-neon-cyan/10 border border-neon-cyan/25 rounded px-1.5 py-0.5">
              {ticket.orderId}
            </span>
          </Row>
        )}

        <Row label="Raised on">
          <span className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
            {formatDate(ticket.createdAt)}
          </span>
        </Row>

        {ticket.updatedAt !== ticket.createdAt && (
          <Row label="Last updated">
            <span className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
              {formatDate(ticket.updatedAt)}
            </span>
          </Row>
        )}

        <div className="pt-1">
          <dt className="text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-1.5">
            Description
          </dt>
          <dd className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary leading-relaxed whitespace-pre-wrap">
            {ticket.description}
          </dd>
        </div>
      </dl>
    </div>
  )
}
