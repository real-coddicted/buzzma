import { Loading } from '../Loading'
import { ClaimProofThumbnailCard } from './ClaimProofThumbnailCard'
import { getProofScore } from './claimUtils'
import type { ClaimProofItem } from './ClaimProofGallery'

interface Props {
  items: ClaimProofItem[]
  loading: boolean
  isAgency: boolean
  activeId: string | null
  onSelect: (item: ClaimProofItem) => void
  onOpenOverlay: (item: ClaimProofItem) => void
  onApprove: (item: ClaimProofItem) => void
}

export function ClaimProofLeftRail({ items, loading, isAgency, activeId, onSelect, onOpenOverlay, onApprove }: Props) {
  return (
    <div className="w-52 flex-shrink-0 border-r border-surface-light-border dark:border-surface-dark-border overflow-y-auto flex flex-col">
      <div className="px-3 py-2 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0 flex items-center justify-between">
        <span className="text-[9.5px] font-bold uppercase tracking-widest text-ink-light-muted dark:text-ink-dark-muted">Screenshots</span>
        <span className="text-[9px] text-ink-light-muted dark:text-ink-dark-muted">Click image to zoom</span>
      </div>

      {loading ? (
        <Loading size={24} className="m-auto mt-8" />
      ) : items.length === 0 ? (
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted text-center py-8">No proof submitted yet.</p>
      ) : (
        <div className="flex flex-col gap-2 p-2.5">
          {items.map((item, idx) => (
            <ClaimProofThumbnailCard
              key={item.id}
              item={item}
              idx={idx}
              isActive={activeId === item.id}
              isAgency={isAgency}
              score={getProofScore(item)}
              onSelect={() => onSelect(item)}
              onOpenOverlay={() => onOpenOverlay(item)}
              onApprove={() => onApprove(item)}
              onOpenRejectOverlay={() => onOpenOverlay(item)}
            />
          ))}
        </div>
      )}
    </div>
  )
}
