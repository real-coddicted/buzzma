import type { TicketStatus } from '../types/TicketTypes'

export const TICKET_STATUS_CONFIG: Record<TicketStatus, { label: string; classes: string }> = {
  Open:       { label: 'Open',        classes: 'text-neon-blue   border-neon-blue/30   bg-neon-blue/10'   },
  InProgress: { label: 'In Progress', classes: 'text-neon-yellow border-neon-yellow/30 bg-neon-yellow/10' },
  Resolved:   { label: 'Resolved',    classes: 'text-neon-green  border-neon-green/30  bg-neon-green/10'  },
  Rejected:   { label: 'Rejected',    classes: 'text-neon-red    border-neon-red/30    bg-neon-red/10'    },
}

export const TICKET_STATUS_FILTERS: { value: TicketStatus | 'all'; label: string; activeClasses: string }[] = [
  { value: 'all',        label: 'All',         activeClasses: 'bg-neon-blue/10   text-neon-blue   border-neon-blue/30'   },
  { value: 'Open',       label: 'Open',        activeClasses: TICKET_STATUS_CONFIG.Open.classes       },
  { value: 'InProgress', label: 'In Progress', activeClasses: TICKET_STATUS_CONFIG.InProgress.classes },
  { value: 'Resolved',   label: 'Resolved',    activeClasses: TICKET_STATUS_CONFIG.Resolved.classes   },
  { value: 'Rejected',   label: 'Rejected',    activeClasses: TICKET_STATUS_CONFIG.Rejected.classes   },
]
