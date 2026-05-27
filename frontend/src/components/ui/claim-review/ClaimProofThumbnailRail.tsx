import { ProductThumbnail } from '../deal/ProductThumbnail'
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
            'w-14 h-14 rounded-lg overflow-hidden border-2 transition-colors flex-shrink-0',
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
        </button>
      ))}
    </div>
  )
}
