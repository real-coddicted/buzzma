import { useState } from 'react'
import { Card } from '../Card'
import { ClaimProofThumbnailRail } from './ClaimProofThumbnailRail'
import { ClaimProofImageLens, LENS_FRACTION } from './ClaimProofImageLens'
import { ClaimProofExtractedData } from './ClaimProofExtractedData'
import { ClaimProofActions } from './ClaimProofActions'
import { ClaimProofScoreBar } from './ClaimProofScoreBar'

export interface ExtractedField {
  label: string
  value: string
  matched: boolean
  score?: number | null
}

export interface ClaimProofItem {
  id: string
  imageUrl: string
  imageAlt?: string
  score?: number
  fields: ExtractedField[]
}

interface ClaimProofGalleryProps {
  items: ClaimProofItem[]
  onApprove: (item: ClaimProofItem) => void
  onRequestProof: (item: ClaimProofItem) => void
  onVerified: (item: ClaimProofItem) => void
  onReject: (item: ClaimProofItem) => void
}

const ZOOM = 1 / LENS_FRACTION

export function ClaimProofGallery({
  items,
  onApprove,
  onRequestProof,
  onVerified,
  onReject,
}: ClaimProofGalleryProps) {
  const [selectedIndex, setSelectedIndex] = useState(0)
  const [hovering, setHovering] = useState(false)
  const [pos, setPos] = useState({ x: 50, y: 50 })

  if (items.length === 0) {
    return (
      <Card padded={false}>
        <div className="flex justify-center py-16 text-ink-light-muted dark:text-ink-dark-muted text-sm">
          No proof submitted yet.
        </div>
      </Card>
    )
  }

  const safeIndex = Math.min(selectedIndex, items.length - 1)
  const selected = items[safeIndex]
  const score = selected.score != null
    ? Math.round(selected.score * 100)
    : selected.fields.length === 0
      ? 0
      : Math.round((selected.fields.filter(f => f.matched).length / selected.fields.length) * 100)

  return (
    <div className="flex flex-col gap-4">
      <Card padded={false}>
        <div className="flex flex-col md:flex-row">
          <ClaimProofThumbnailRail
            items={items}
            selectedIndex={safeIndex}
            onSelect={setSelectedIndex}
          />

          <ClaimProofImageLens
            imageUrl={selected.imageUrl}
            imageAlt={selected.imageAlt}
            hovering={hovering}
            pos={pos}
            onHoverChange={setHovering}
            onPosChange={setPos}
          />

          {/* right pane — zoom on hover, extracted data otherwise */}
          <div className="flex-1 md:border-l border-t md:border-t-0 border-surface-light-border dark:border-surface-dark-border min-w-0">
            {hovering ? (
              <div
                className="w-full h-full min-h-[20rem] bg-no-repeat bg-surface-light-hover dark:bg-surface-dark-hover"
                style={{
                  backgroundImage: `url(${selected.imageUrl})`,
                  backgroundSize: `${ZOOM * 100}% ${ZOOM * 100}%`,
                  backgroundPosition: `${pos.x}% ${pos.y}%`,
                }}
              />
            ) : (
              <div className="p-5 flex flex-col gap-4 h-full">
                <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                  Extracted Data
                </h3>
                <ClaimProofExtractedData fields={selected.fields} />
                <div className="mt-auto">
                  <ClaimProofScoreBar score={score} />
                </div>
              </div>
            )}
          </div>
        </div>
      </Card>

      <ClaimProofActions
        onApprove={() => onApprove(selected)}
        onRequestProof={() => onRequestProof(selected)}
        onVerified={() => onVerified(selected)}
        onReject={() => onReject(selected)}
      />
    </div>
  )
}
