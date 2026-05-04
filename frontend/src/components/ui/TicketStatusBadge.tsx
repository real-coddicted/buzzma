import { TICKET_STATUS_CONFIG } from '../../constants/ticketStatus'
import type { TicketStatus } from '../../types/TicketTypes'

interface Props {
  status: TicketStatus
}

export function TicketStatusBadge({ status }: Props) {
  const { label, classes } = TICKET_STATUS_CONFIG[status]
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold border ${classes}`}>
      {label}
    </span>
  )
}
