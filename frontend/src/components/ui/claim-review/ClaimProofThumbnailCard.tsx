import { IconCheck, IconX } from '../icons'
import { SCREENSHOT_TYPE_CONFIG } from './claimReviewConstants'
import type { ClaimProofItem } from './ClaimProofGallery'

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
  isAgency: boolean
  score: number
  onSelect: () => void
  onOpenOverlay: () => void
  onApprove: () => void
  onOpenRejectOverlay: () => void
}

export function ClaimProofThumbnailCard({
  item, idx, isActive, isAgency, score,
  onSelect, onOpenOverlay, onApprove, onOpenRejectOverlay,
}: Props) {
  const sc = SCREENSHOT_TYPE_CONFIG[item.type ?? '']
  const isVerified = item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED'
  const isRejected = item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'
  const isActioned = isVerified || isRejected

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

      {/* Agency actions */}
      {isAgency && (
        <div className="border-t border-surface-light-border dark:border-surface-dark-border px-2.5 py-2">
          {isActioned ? (
            <span className={[
              'w-full flex items-center justify-center gap-1 text-[10px] font-semibold py-0.5',
              isVerified ? 'text-neon-green' : 'text-neon-red',
            ].join(' ')}>
              {isVerified
                ? <><IconCheck size={9} /> Verified</>
                : <><IconX size={9} /> Rejected</>}
            </span>
          ) : (
            <div className="flex gap-1.5">
              <button
                onClick={e => { e.stopPropagation(); onApprove() }}
                className="flex-1 flex items-center justify-center gap-1 py-1 rounded-lg text-[10px] font-semibold bg-neon-green/10 text-neon-green border border-neon-green/25 hover:bg-neon-green/20 transition-colors"
              >
                <IconCheck size={9} /> Approve
              </button>
              <button
                onClick={e => { e.stopPropagation(); onOpenRejectOverlay() }}
                className="flex-1 flex items-center justify-center gap-1 py-1 rounded-lg text-[10px] font-semibold bg-neon-red/10 text-neon-red border border-neon-red/25 hover:bg-neon-red/20 transition-colors"
              >
                <IconX size={9} /> Reject
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
