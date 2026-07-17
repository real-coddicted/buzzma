import { IconPause, IconPlay, IconCopy, IconEdit, IconX, IconEye, IconTrash } from '../icons'
import type { Campaign } from '../../../types'

interface Props {
  campaign: Campaign
  onEdit: () => void
  onCopy: () => void
  onView: () => void
  onPause: () => void
  onResume: () => void
  onClose: () => void
  onDelete: () => void
}

const btn = {
  view:   'p-1.5 rounded-lg text-neon-blue   bg-neon-blue/10   hover:bg-neon-blue/20   transition-colors',
  edit:   'p-1.5 rounded-lg text-neon-blue   bg-neon-blue/10   hover:bg-neon-blue/20   transition-colors',
  pause:  'p-1.5 rounded-lg text-neon-yellow bg-neon-yellow/10 hover:bg-neon-yellow/20 transition-colors',
  resume: 'p-1.5 rounded-lg text-neon-green  bg-neon-green/10  hover:bg-neon-green/20  transition-colors',
  close:  'p-1.5 rounded-lg text-neon-red    bg-neon-red/10    hover:bg-neon-red/20    transition-colors',
  copy:   'p-1.5 rounded-lg text-neon-cyan   bg-neon-cyan/10   hover:bg-neon-cyan/20   transition-colors',
  delete: 'p-1.5 rounded-lg text-neon-red    bg-neon-red/10    hover:bg-neon-red/20    transition-colors',
}

export function CampaignRowActions({ campaign: c, onEdit, onCopy, onView, onPause, onResume, onClose, onDelete }: Props) {
  return (
    <div className="flex items-center justify-end gap-1">
      <div className="flex items-center gap-1">
        {c.status === 'draft' && (
          <button title="Edit" onClick={onEdit} className={btn.edit}>
            <IconEdit size={14} />
          </button>
        )}
        {c.status === 'draft' && (
          <button title="Delete" onClick={onDelete} className={btn.delete}>
            <IconTrash size={14} />
          </button>
        )}
        {(c.status === 'active' || c.status === 'paused' || c.status === 'closed' || c.status === 'completed') && (
          <button title="View" onClick={onView} className={btn.view}>
            <IconEye size={14} />
          </button>
        )}
        {c.status === 'active' && (
          <button title="Pause" onClick={onPause} className={btn.pause}>
            <IconPause size={14} />
          </button>
        )}
        {c.status === 'paused' && (
          <button title="Resume" onClick={onResume} className={btn.resume}>
            <IconPlay size={14} />
          </button>
        )}
        {(c.status === 'active' || c.status === 'paused') && (
          <button title="Close" onClick={onClose} className={btn.close}>
            <IconX size={14} />
          </button>
        )}
      </div>
      <button title="Copy" onClick={onCopy} className={btn.copy}>
        <IconCopy size={14} />
      </button>
    </div>
  )
}
