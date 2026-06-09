import { IconFilter } from '../icons'
import { TICKET_STATUS_FILTERS } from '../../../constants/ticketStatus'
import type { TicketStatus } from '../../../types/TicketTypes'

interface Props {
  value: TicketStatus | 'all'
  onChange: (value: TicketStatus | 'all') => void
}

export function TicketFilterBar({ value, onChange }: Props) {
  return (
    <div className="flex items-center gap-1.5 flex-wrap">
      <IconFilter size={13} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
      {TICKET_STATUS_FILTERS.map(f => (
        <button
          key={f.value}
          onClick={() => onChange(f.value)}
          className={[
            'px-3 py-1 rounded-full text-xs font-medium border transition-all',
            value === f.value
              ? f.activeClasses
              : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
          ].join(' ')}
        >
          {f.label}
        </button>
      ))}
    </div>
  )
}
