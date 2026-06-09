import { useState } from 'react'
import { Card } from '../Card'
import { Toast } from '../Toast'
import { updateTicketStatus } from '../../../api/ticketApi'
import type { Ticket } from '../../../types/TicketTypes'
import type { components } from '../../../types/api'

type TicketAction = components['schemas']['TicketStatusUpdateRequestDto']['action']

interface Props {
  ticket: Ticket
  role: 'reporter' | 'assignee'
  onUpdate: (updated: Ticket) => void
}

export function TicketActions({ ticket, role, onUpdate }: Props) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { status } = ticket

  async function handle(action: TicketAction) {
    setBusy(true)
    setError(null)
    try {
      onUpdate(await updateTicketStatus(ticket.id, action))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Action failed. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  const isTerminal = status === 'Closed'
  const hasActions = role === 'reporter' ? !isTerminal : (status !== 'Resolved' && !isTerminal)

  const reporterButtons = (
    <>
      {/* Close — available at any non-terminal stage */}
      {!isTerminal && (
        <button
          disabled={busy}
          onClick={() => handle('TICKET_ACTION_CLOSE')}
          className="px-3 py-1.5 rounded-lg text-xs font-medium bg-neon-red/10 text-neon-red border border-neon-red/30 hover:bg-neon-red/20 disabled:opacity-50 transition-colors"
        >
          Close
        </button>
      )}
      {/* Info Provided — only when waiting for reporter's input */}
      {status === 'WaitingForUser' && (
        <button
          disabled={busy}
          onClick={() => handle('TICKET_ACTION_INFO_PROVIDED')}
          className="px-3 py-1.5 rounded-lg text-xs font-medium bg-neon-cyan/10 text-neon-cyan border border-neon-cyan/30 hover:bg-neon-cyan/20 disabled:opacity-50 transition-colors"
        >
          Info Provided
        </button>
      )}
      {/* Reopen — only when resolved */}
      {status === 'Resolved' && (
        <button
          disabled={busy}
          onClick={() => handle('TICKET_ACTION_REOPEN')}
          className="px-3 py-1.5 rounded-lg text-xs font-medium bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-secondary dark:text-ink-dark-secondary border border-surface-light-border dark:border-surface-dark-border hover:bg-surface-light-raised dark:hover:bg-surface-dark-raised disabled:opacity-50 transition-colors"
        >
          Reopen
        </button>
      )}
    </>
  )

  const assigneeButtons = (
    <>
      {/* Mark Resolved — not when already resolved or closed */}
      {status !== 'Resolved' && !isTerminal && (
        <button
          disabled={busy}
          onClick={() => handle('TICKET_ACTION_MARK_RESOLVE')}
          className="px-3 py-1.5 rounded-lg text-xs font-medium bg-neon-green/10 text-neon-green border border-neon-green/30 hover:bg-neon-green/20 disabled:opacity-50 transition-colors"
        >
          Mark Resolved
        </button>
      )}
      {/* Request More Info — not when resolved, closed, or already waiting */}
      {status !== 'Resolved' && !isTerminal && status !== 'WaitingForUser' && (
        <button
          disabled={busy}
          onClick={() => handle('TICKET_ACTION_REQUEST_ADDITIONAL_INFO')}
          className="px-3 py-1.5 rounded-lg text-xs font-medium bg-neon-cyan/10 text-neon-cyan border border-neon-cyan/30 hover:bg-neon-cyan/20 disabled:opacity-50 transition-colors"
        >
          Request More Info
        </button>
      )}
    </>
  )

  return (
    <>
      <Card>
        <div className="flex items-center gap-2 flex-wrap">
          <span className="text-xs font-semibold text-ink-light-muted dark:text-ink-dark-muted mr-1">Actions</span>
          {hasActions
            ? (role === 'reporter' ? reporterButtons : assigneeButtons)
            : <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">No action available</span>
          }
        </div>
      </Card>
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </>
  )
}
