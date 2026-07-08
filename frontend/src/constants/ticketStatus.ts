import type { TicketStatus } from '../types/TicketTypes'

export const TICKET_STATUS_CONFIG: Record<TicketStatus, { label: string; classes: string }> = {
  InProgress:     { label: 'In Progress',       classes: 'text-neon-yellow border-neon-yellow/30 bg-neon-yellow/10' },
  WaitingForUser: { label: 'Waiting for User',  classes: 'text-neon-cyan   border-neon-cyan/30   bg-neon-cyan/10'   },
  Resolved:       { label: 'Resolved',          classes: 'text-neon-green  border-neon-green/30  bg-neon-green/10'  },
  Closed:         { label: 'Closed',            classes: 'text-neon-red    border-neon-red/30    bg-neon-red/10'    },
}

export const TICKET_STATUS_FILTERS: { value: TicketStatus | 'all'; label: string; activeClasses: string }[] = [
  { value: 'all',           label: 'All',             activeClasses: 'bg-neon-blue/10   text-neon-blue   border-neon-blue/30'   },
  { value: 'InProgress',    label: 'In Progress',     activeClasses: TICKET_STATUS_CONFIG.InProgress.classes     },
  { value: 'WaitingForUser',label: 'Waiting for User',activeClasses: TICKET_STATUS_CONFIG.WaitingForUser.classes },
  { value: 'Resolved',      label: 'Resolved',        activeClasses: TICKET_STATUS_CONFIG.Resolved.classes       },
  { value: 'Closed',        label: 'Closed',          activeClasses: TICKET_STATUS_CONFIG.Closed.classes         },
]
