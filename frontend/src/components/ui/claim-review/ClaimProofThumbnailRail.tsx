import { ProductThumbnail } from '../deal/ProductThumbnail'
import { IconCheck, IconX } from '../icons'
import type { ClaimProofItem } from './ClaimProofGallery'

interface ClaimProofThumbnailRailProps {
  items: ClaimProofItem[]
  selectedIndex: number
  onSelect: (index: number) => void
}

export function ClaimProofThumbnailRail({
  items,
  selectedIndex,
  onSelect,
}: ClaimProofThumbnailRailProps) {
  return (
    <div className="flex md:flex-col gap-2 p-3 md:border-r border-b md:border-b-0 border-surface-light-border dark:border-surface-dark-border md:max-h-[36rem] overflow-x-auto md:overflow-x-visible md:overflow-y-auto">
      {items.map((item, i) => (
        <button
          key={item.id}
          type="button"
          onClick={() => onSelect(i)}
          title={item.imageAlt}
          className={[
            'relative w-14 h-14 rounded-lg overflow-hidden border-2 transition-colors flex-shrink-0',
            i === selectedIndex
              ? 'border-neon-blue'
              : 'border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/50',
          ].join(' ')}
        >
          <ProductThumbnail
            src={item.imageUrl}
            alt={item.imageAlt ?? ''}
            className="w-full h-full"
          />
          {item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' && (
            <div className="absolute inset-0 flex items-center justify-center bg-neon-green/20">
              <div className="rounded-full bg-neon-green p-1 flex items-center justify-center">
                <IconCheck size={14} className="text-white" />
              </div>
            </div>
          )}
          {item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED' && (
            <div className="absolute inset-0 flex items-center justify-center bg-neon-red/20">
              <div className="rounded-full bg-neon-red p-1 flex items-center justify-center">
                <IconX size={14} className="text-white" />
              </div>
            </div>
          )}
        </button>
      ))}
    </div>
  )
}
