import { IconCheck, IconX, IconEdit } from '../icons'
import { SCREENSHOT_TYPE_CONFIG } from './claimReviewConstants'
import type { ClaimProofItem } from './ClaimProofGallery'
import { canReviewClaims } from './claimUtils'

function scoreColor(score: number) {
  if (score >= 80) return 'text-neon-green'
  if (score >= 50) return 'text-neon-yellow'
  if (score > 0)   return 'text-neon-red'
  return 'text-ink-light-muted dark:text-ink-dark-muted'
}

function scoreBarColor(score: number) {
  if (score >= 80) return 'bg-neon-green'
  if (score >= 50) return 'bg-neon-yellow'
  return 'bg-neon-red'
}

interface Props {
  item: ClaimProofItem
  idx: number
  isActive: boolean
  userRole: string | undefined
  score: number
  onSelect: () => void
  onOpenOverlay: () => void
}

export function ClaimProofThumbnailCard({
  item, idx, isActive, userRole, score,
  onSelect, onOpenOverlay,
}: Props) {
  const sc = SCREENSHOT_TYPE_CONFIG[item.type ?? '']
  const canReview = canReviewClaims(userRole)
  const isVerified = item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED'
  const isRejected = item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'

  return (
    <div
      onClick={onSelect}
      className={[
        'flex flex-col rounded-xl border cursor-pointer overflow-hidden',
        'bg-surface-light-card dark:bg-surface-dark-card',
        'shadow-sm transition-all duration-150 hover:-translate-y-0.5 hover:shadow-md',
        isActive
          ? 'border-neon-blue/50'
          : isVerified
            ? 'border-neon-green/40'
            : isRejected
              ? 'border-neon-red/40'
              : 'border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/30',
      ].join(' ')}
    >
      {/* Image zone — clicking opens the overlay */}
      <button
        onClick={e => { e.stopPropagation(); onOpenOverlay() }}
        className="relative block w-full overflow-hidden group"
        style={{ aspectRatio: '16/10' }}
        title="Click to zoom and compare"
      >
        <img
          src={item.imageUrl}
          alt={item.imageAlt ?? `Screenshot ${idx + 1}`}
          className="w-full h-full object-cover"
        />
        {sc && (
          <span className={[
            'absolute top-1.5 left-1.5 text-[7.5px] font-extrabold px-1.5 py-0.5 rounded uppercase tracking-widest',
            sc.tagClass,
          ].join(' ')}>
            {sc.tag}
          </span>
        )}
        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-colors flex items-center justify-center">
          <span className="opacity-0 group-hover:opacity-100 transition-opacity text-white text-[10px] font-semibold bg-black/60 px-2.5 py-1 rounded">
            ⤢ Zoom &amp; Compare
          </span>
        </div>
      </button>

      {/* Label + score */}
      <div className="flex items-center justify-between gap-1.5 px-2.5 pt-2 pb-0">
        <span className="text-[10.5px] font-semibold text-ink-light-primary dark:text-ink-dark-primary truncate">
          {sc?.label ?? `Screenshot ${idx + 1}`}
        </span>
        <span className={['text-[10.5px] font-extrabold flex-shrink-0', scoreColor(score)].join(' ')}>
          {score > 0 ? `${score}%` : '—'}
        </span>
      </div>

      {/* Score bar (compact) */}
      <div className="px-2.5 pt-1.5 pb-2.5">
        <div className="h-1 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
          <div
            className={['h-full rounded-full transition-all', scoreBarColor(score)].join(' ')}
            style={{ width: `${score}%` }}
          />
        </div>
      </div>

      {/* Status badge + review affordance */}
      <div className="border-t border-surface-light-border dark:border-surface-dark-border px-2.5 py-2 flex items-center justify-between gap-1.5">
        <span className={[
          'flex items-center gap-1 text-[10px] font-semibold',
          isVerified ? 'text-neon-green' : isRejected ? 'text-neon-red' : 'text-ink-light-muted dark:text-ink-dark-muted',
        ].join(' ')}>
          {isVerified && <><IconCheck size={9} /> Verified</>}
          {isRejected && <><IconX size={9} /> Rejected</>}
          {!isVerified && !isRejected && 'Pending'}
        </span>
        {canReview && (
          <button
            onClick={e => { e.stopPropagation(); onOpenOverlay() }}
            className="flex items-center gap-1 text-[10px] font-semibold text-neon-blue hover:underline"
          >
            <IconEdit size={9} /> Review
          </button>
        )}
      </div>
    </div>
  )
}
