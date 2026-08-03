import { IconEye, IconCheck, IconX } from '../icons'
import type { ClaimReviewItem } from '../../../types'

interface ActionButtonProps {
  title: string
  colorClass: string
  onClick: () => void
  icon: React.ReactNode
  disabled?: boolean
}

function ActionButton({ title, colorClass, onClick, icon, disabled }: ActionButtonProps) {
  return (
    <button
      title={title}
      onClick={onClick}
      disabled={disabled}
      className={[
        'p-1.5 rounded-lg border transition-colors',
        disabled ? 'opacity-40 cursor-not-allowed pointer-events-none' : '',
        colorClass,
      ].join(' ')}
    >
      {icon}
    </button>
  )
}

interface ClaimReviewActionsProps {
  row: ClaimReviewItem
  onAction: (action: string, row: ClaimReviewItem) => void
  showReject?: boolean
}

export function ClaimReviewActions({ row, onAction, showReject }: ClaimReviewActionsProps) {
  const eligible = !!(row.isUnderReview || row.isUnderBrandReview)
  return (
    <div className="flex items-center justify-center gap-1">
      <ActionButton
        title="View Details"
        colorClass="text-ink-light-muted dark:text-ink-dark-muted border-surface-light-border dark:border-surface-dark-border hover:text-neon-purple hover:border-neon-purple/30 hover:bg-neon-purple/10"
        onClick={() => onAction('details', row)}
        icon={<IconEye size={13} />}
      />
      <ActionButton
        title="Approve"
        colorClass="text-neon-green border-neon-green/30 bg-neon-green/10 hover:bg-neon-green/20"
        onClick={() => onAction('approve', row)}
        icon={<IconCheck size={13} />}
        disabled={!eligible}
      />
      {showReject && (
        <ActionButton
          title="Reject"
          colorClass="text-neon-red border-neon-red/30 bg-neon-red/10 hover:bg-neon-red/20"
          onClick={() => onAction('reject', row)}
          icon={<IconX size={13} />}
          disabled={!eligible}
        />
      )}
    </div>
  )
}
