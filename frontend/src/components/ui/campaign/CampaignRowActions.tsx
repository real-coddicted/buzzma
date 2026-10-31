import { IconPause, IconPlay, IconCopy, IconEdit, IconX, IconEye, IconTrash, IconList } from '../icons'
import type { Campaign } from '../../../types'
import { VIEW_ONLY_TOOLTIP, useViewOnly } from '../../../contexts/ViewOnlyContext'

interface Props {
  campaign: Campaign
  onEdit: () => void
  onCopy: () => void
  onView: () => void
  onPause: () => void
  onResume: () => void
  onClose: () => void
  onDelete: () => void
  onViewClaims: () => void
  /** When true, renders only the "View Claims" action (used for read-only viewers, e.g. a Brand viewing campaigns shared with them). */
  readOnly?: boolean
}

const btn = {
  view:   'p-1.5 rounded-lg text-neon-blue   bg-neon-blue/10   hover:bg-neon-blue/20   transition-colors',
  edit:   'p-1.5 rounded-lg text-neon-blue   bg-neon-blue/10   hover:bg-neon-blue/20   transition-colors',
  pause:  'p-1.5 rounded-lg text-neon-yellow bg-neon-yellow/10 hover:bg-neon-yellow/20 transition-colors',
  resume: 'p-1.5 rounded-lg text-neon-green  bg-neon-green/10  hover:bg-neon-green/20  transition-colors',
  close:  'p-1.5 rounded-lg text-neon-red    bg-neon-red/10    hover:bg-neon-red/20    transition-colors',
  copy:   'p-1.5 rounded-lg text-neon-cyan   bg-neon-cyan/10   hover:bg-neon-cyan/20   transition-colors',
  delete: 'p-1.5 rounded-lg text-neon-red    bg-neon-red/10    hover:bg-neon-red/20    transition-colors',
  claims: 'p-1.5 rounded-lg text-neon-purple bg-neon-purple/10 hover:bg-neon-purple/20 transition-colors',
}

const disabledClass = ' disabled:opacity-50 disabled:cursor-not-allowed'

export function CampaignRowActions({ campaign: c, onEdit, onCopy, onView, onPause, onResume, onClose, onDelete, onViewClaims, readOnly = false }: Props) {
  // Viewing as another user: changes are disabled; Edit still opens the form, read-only.
  const viewOnly = useViewOnly()
  const writeProps = (title: string) => ({ disabled: viewOnly, title: viewOnly ? VIEW_ONLY_TOOLTIP : title })
  if (readOnly) {
    return (
      <div className="flex items-center justify-end gap-1">
        {(c.status === 'active' || c.status === 'paused' || c.status === 'closed' || c.status === 'completed') && (
          <button title="View Claims" onClick={onViewClaims} className={btn.claims}>
            <IconList size={14} />
          </button>
        )}
      </div>
    )
  }

  return (
    <div className="flex items-center justify-end gap-1">
      <div className="flex items-center gap-1">
        {c.status === 'draft' && (
          <button title="Edit" onClick={onEdit} className={btn.edit}>
            <IconEdit size={14} />
          </button>
        )}
        {c.status === 'draft' && (
          <button {...writeProps('Delete')} onClick={onDelete} className={btn.delete + disabledClass}>
            <IconTrash size={14} />
          </button>
        )}
        {(c.status === 'active' || c.status === 'paused' || c.status === 'closed' || c.status === 'completed') && (
          <button title="View" onClick={onView} className={btn.view}>
            <IconEye size={14} />
          </button>
        )}
        {(c.status === 'active' || c.status === 'paused' || c.status === 'closed' || c.status === 'completed') && (
          <button title="View Claims" onClick={onViewClaims} className={btn.claims}>
            <IconList size={14} />
          </button>
        )}
        {c.status === 'active' && (
          <button {...writeProps('Pause')} onClick={onPause} className={btn.pause + disabledClass}>
            <IconPause size={14} />
          </button>
        )}
        {c.status === 'paused' && (
          <button {...writeProps('Resume')} onClick={onResume} className={btn.resume + disabledClass}>
            <IconPlay size={14} />
          </button>
        )}
        {(c.status === 'active' || c.status === 'paused') && (
          <button {...writeProps('Close')} onClick={onClose} className={btn.close + disabledClass}>
            <IconX size={14} />
          </button>
        )}
      </div>
      <button {...writeProps('Copy')} onClick={onCopy} className={btn.copy + disabledClass}>
        <IconCopy size={14} />
      </button>
    </div>
  )
}
