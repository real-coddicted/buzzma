import { useState, useEffect } from 'react'
import { TicketStatusBadge } from './TicketStatusBadge'
import { fetchTicketComments } from '../../api/ticketApi'
import type { Ticket, TicketComment } from '../../types/TicketTypes'

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
  })
}

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

interface Props {
  ticket: Ticket
  onBack: () => void
}

export function TicketDetail({ ticket, onBack }: Props) {
  const [comments, setComments] = useState<TicketComment[]>([])
  const [commentsLoading, setCommentsLoading] = useState(true)
  const [commentsError, setCommentsError] = useState(false)

  useEffect(() => {
    setCommentsLoading(true)
    setCommentsError(false)
    fetchTicketComments(ticket.id)
      .then(setComments)
      .catch(() => setCommentsError(true))
      .finally(() => setCommentsLoading(false))
  }, [ticket.id])

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
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Ticket <span className="font-mono text-ink-light-muted dark:text-ink-dark-muted text-base">#{ticket.id}</span>
          </h1>
        </div>
      </div>

      {/* Two-column layout — fills remaining viewport height */}
      {/* 64px topbar + ~56px page header + 24px top padding + 16px gap = ~160px offset */}
      <div
        className="grid grid-cols-1 lg:grid-cols-2 gap-4"
        style={{ height: 'calc(100vh - 160px)' }}
      >
        {/* Left — Ticket details (scrollable) */}
        <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-y-auto p-5">
          <div className="space-y-4">
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
        </div>

        {/* Right — Comments (scrollable, full height) */}
        <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card flex flex-col overflow-hidden">
          {/* Header */}
          <div className="px-5 py-4 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
            <h2 className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">
              Comments
              {!commentsLoading && !commentsError && (
                <span className="ml-2 text-[11px] font-normal text-ink-light-muted dark:text-ink-dark-muted">
                  {comments.length}
                </span>
              )}
            </h2>
          </div>

          {/* Scrollable comment list */}
          <div className="flex-1 overflow-y-auto px-5 py-4">
            {commentsLoading ? (
              <div className="space-y-3">
                {[1, 2, 3].map(i => (
                  <div key={i} className="h-14 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover animate-pulse" />
                ))}
              </div>
            ) : commentsError ? (
              <p className="text-xs text-neon-red">Failed to load comments.</p>
            ) : comments.length === 0 ? (
              <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted py-4 text-center">
                No comments yet.
              </p>
            ) : (
              <div className="space-y-3">
                {comments.map(c => {
                  const isSupport = c.role === 'support'
                  return (
                    <div
                      key={c.id}
                      className={[
                        'rounded-lg px-3 py-2.5 border text-xs',
                        isSupport
                          ? 'bg-neon-blue/5 border-neon-blue/20'
                          : 'bg-surface-light-hover dark:bg-surface-dark-hover border-surface-light-border dark:border-surface-dark-border',
                      ].join(' ')}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className={['font-semibold', isSupport ? 'text-neon-blue' : 'text-ink-light-primary dark:text-ink-dark-primary'].join(' ')}>
                          {c.userName}
                        </span>
                        <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                          {formatDateTime(c.createdAt)}
                        </span>
                      </div>
                      <p className="text-ink-light-secondary dark:text-ink-dark-secondary leading-relaxed">
                        {c.message}
                      </p>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
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
