import { useRef, useState } from 'react'
import { ClaimProofLeftRail } from './ClaimProofLeftRail'
import { ClaimProofRightPanel } from './ClaimProofRightPanel'
import { ClaimProofScreenshotOverlay } from './ClaimProofScreenshotOverlay'
import { getProofScore } from './claimUtils'
import type { ClaimProofItem } from './ClaimProofGallery'
import type { ClaimReviewItem } from '../../../types'

interface Props {
  items: ClaimProofItem[]
  loading: boolean
  userRole: string | undefined
  claim: ClaimReviewItem
  campaignTitle?: string
  onApproveScreenshot: (item: ClaimProofItem) => void
  onRejectScreenshot: (item: ClaimProofItem, comment: string) => void
  onApproveClaim: (comment: string) => void
  onVerifiedClaim: () => void
  onRejectClaim: (comment: string) => void
}

export function ClaimProofUnified({
  items,
  loading,
  userRole,
  claim,
  campaignTitle,
  onApproveScreenshot,
  onRejectScreenshot,
  onApproveClaim,
  onVerifiedClaim,
  onRejectClaim,
}: Props) {
  const [activeId, setActiveId] = useState<string | null>(null)
  const [overlayItem, setOverlayItem] = useState<ClaimProofItem | null>(null)
  const sectionRefs = useRef<Record<string, HTMLElement | null>>({})

  const effectiveActiveId = activeId ?? items[0]?.id ?? null

  function handleRailSelect(item: ClaimProofItem) {
    setActiveId(item.id)
    sectionRefs.current[item.id]?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  function openOverlay(item: ClaimProofItem) {
    setActiveId(item.id)
    setOverlayItem(item)
  }

  const overlayIdx = overlayItem ? items.findIndex(i => i.id === overlayItem.id) : -1

  function handleOverlayNav(dir: -1 | 1) {
    const next = items[overlayIdx + dir]
    if (next) {
      setActiveId(next.id)
      setOverlayItem(next)
    }
  }

  return (
    <div className="flex h-[calc(100vh-5.5rem)] overflow-hidden rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-base dark:bg-surface-dark-base">
      <ClaimProofLeftRail
        items={items}
        loading={loading}
        userRole={userRole}
        activeId={effectiveActiveId}
        onSelect={handleRailSelect}
        onOpenOverlay={openOverlay}
        onApprove={onApproveScreenshot}
      />
      <ClaimProofRightPanel
        items={items}
        loading={loading}
        activeId={effectiveActiveId}
        sectionRefs={sectionRefs}
        claim={claim}
        campaignTitle={campaignTitle}
        userRole={userRole}
        onOpenOverlay={openOverlay}
        onApproveClaim={onApproveClaim}
        onVerifiedClaim={onVerifiedClaim}
        onRejectClaim={onRejectClaim}
      />
      {overlayItem && (
        <ClaimProofScreenshotOverlay
          item={overlayItem}
          idx={overlayIdx}
          score={getProofScore(overlayItem)}
          userRole={userRole}
          hasPrev={overlayIdx > 0}
          hasNext={overlayIdx < items.length - 1}
          onPrev={() => handleOverlayNav(-1)}
          onNext={() => handleOverlayNav(1)}
          onClose={() => setOverlayItem(null)}
          onApprove={() => {
            onApproveScreenshot(overlayItem)
            setOverlayItem(null)
          }}
          onReject={comment => {
            onRejectScreenshot(overlayItem, comment)
            setOverlayItem(null)
          }}
        />
      )}
    </div>
  )
}
