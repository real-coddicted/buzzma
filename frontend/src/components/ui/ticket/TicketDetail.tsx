import { useEffect } from 'react'
import { TicketInfo } from './TicketInfo'
import { TicketActivity } from './TicketActivity'
import { TicketComments } from './TicketComments'
import { TicketActions } from './TicketActions'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import type { Ticket } from '../../../types/TicketTypes'

interface Props {
  ticket: Ticket
  onBack: () => void
  onUpdate: (updated: Ticket) => void
  showActions?: boolean
  role?: 'reporter' | 'assignee'
}

export function TicketDetail({ ticket, onBack, onUpdate, showActions = false, role = 'assignee' }: Props) {
  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(`#${ticket.id}`, onBack)
    return clearDetail
  }, [ticket.id, onBack, setDetail, clearDetail])

  return (
    <div className="max-w-7xl mx-auto space-y-4">
      <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
        Ticket <span className="font-mono text-ink-light-muted dark:text-ink-dark-muted text-base">#{ticket.id}</span>
      </h1>

      {showActions && <TicketActions ticket={ticket} role={role} onUpdate={onUpdate} />}

      <div
        className="grid grid-cols-1 lg:grid-cols-3 gap-4"
        style={{ height: 'calc(100vh - 224px)' }}
      >
        <TicketInfo ticket={ticket} />
        <TicketActivity ticket={ticket} />
        <TicketComments ticket={ticket} />
      </div>
    </div>
  )
}
