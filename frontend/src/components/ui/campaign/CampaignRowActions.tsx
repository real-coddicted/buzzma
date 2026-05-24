import { Badge } from '../Badge'
import { IconPause, IconCopy, IconEdit, IconX } from '../icons'
import type { Campaign } from '../../../types'

interface Props {
  campaign: Campaign
  onEdit: () => void
  onCopy: () => void
}

export function CampaignRowActions({ campaign: c, onEdit, onCopy }: Props) {
  return (
    <div className="flex items-center justify-end gap-1">
      <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
        {c.status === 'draft' && (
          <button
            title="Edit"
            onClick={onEdit}
            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-blue hover:bg-neon-blue/10 transition-colors"
          >
            <IconEdit size={13} />
          </button>
        )}
        {c.status === 'active' && (
          <>
            <button
              title="Pause"
              className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-yellow hover:bg-neon-yellow/10 transition-colors"
            >
              <IconPause size={13} />
            </button>
            <button
              title="Close"
              className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-red hover:bg-neon-red/10 transition-colors"
            >
              <IconX size={13} />
            </button>
          </>
        )}
      </div>
      <button
        title="Copy"
        onClick={onCopy}
        className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-cyan hover:bg-neon-cyan/10 transition-colors"
      >
        <IconCopy size={13} />
      </button>
      <Badge variant="neutral">
        {c.totalSlots ? Math.round((c.slotsClaimed / c.totalSlots) * 100) : 0}%
      </Badge>
    </div>
  )
}
