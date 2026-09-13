import { useEffect, useRef, useState } from 'react'
import { ClaimProofLeftRail } from './ClaimProofLeftRail'
import { ClaimProofRightPanel } from './ClaimProofRightPanel'
import { ClaimProofScreenshotOverlay } from './ClaimProofScreenshotOverlay'
import { getProofScore, isClaimLocked } from './claimUtils'
import type { ClaimProofItem } from './ClaimProofGallery'
import type { ClaimReviewItem, ScreenshotVerificationStatus } from '../../../types'

interface Props {
  items: ClaimProofItem[]
  loading: boolean
  userRole: string | undefined
  claim: ClaimReviewItem
  campaignTitle?: string
  campaignPricePaise?: number
  isExchangeCampaign?: boolean
  onReviewScreenshot: (item: ClaimProofItem, status: ScreenshotVerificationStatus, comment?: string) => void
  onApproveClaim: (comment: string, amountApprovedPaise?: number) => void
  onVerifiedClaim: () => void
  onBrandVerifiedClaim: () => void
  onRejectClaim: (comment: string) => void
  onResetClaim: () => void
}

export function ClaimProofUnified({
  items,
  loading,
  userRole,
  claim,
  campaignTitle,
  campaignPricePaise,
  isExchangeCampaign,
  onReviewScreenshot,
  onApproveClaim,
  onVerifiedClaim,
  onBrandVerifiedClaim,
  onRejectClaim,
  onResetClaim,
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
  const claimLocked = isClaimLocked(claim.claimStatus)

  // Keep the open overlay's item in sync with the latest fetched data (e.g. after a review
  // submission refetches `items` with new object references), so the status dropdown and the
  // "already matches server value" submit guard reflect the just-saved value.
  useEffect(() => {
    if (overlayItem && overlayIdx >= 0 && items[overlayIdx] !== overlayItem) {
      setOverlayItem(items[overlayIdx])
    }
  }, [items, overlayItem, overlayIdx])

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
      />
      <ClaimProofRightPanel
        items={items}
        loading={loading}
        activeId={effectiveActiveId}
        sectionRefs={sectionRefs}
        claim={claim}
        campaignTitle={campaignTitle}
        campaignPricePaise={campaignPricePaise}
        isExchangeCampaign={isExchangeCampaign}
        userRole={userRole}
        onOpenOverlay={openOverlay}
        onApproveClaim={onApproveClaim}
        onVerifiedClaim={onVerifiedClaim}
        onBrandVerifiedClaim={onBrandVerifiedClaim}
        onRejectClaim={onRejectClaim}
        onResetClaim={onResetClaim}
      />
      {overlayItem && (
        <ClaimProofScreenshotOverlay
          item={overlayItem}
          idx={overlayIdx}
          score={getProofScore(overlayItem)}
          userRole={userRole}
          claimLocked={claimLocked}
          hasPrev={overlayIdx > 0}
          hasNext={overlayIdx < items.length - 1}
          onPrev={() => handleOverlayNav(-1)}
          onNext={() => handleOverlayNav(1)}
          onClose={() => setOverlayItem(null)}
          onSubmitReview={(status, comment) => onReviewScreenshot(overlayItem, status, comment)}
        />
      )}
    </div>
  )
}
