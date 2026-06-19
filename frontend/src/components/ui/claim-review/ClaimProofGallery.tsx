import { useState } from 'react'
import { Card } from '../Card'
import { Button } from '../Button'
import { IconCheck, IconX, IconInfo } from '../icons'
import { ClaimProofThumbnailRail } from './ClaimProofThumbnailRail'
import { ClaimProofImageLens, LENS_FRACTION } from './ClaimProofImageLens'
import { ClaimProofExtractedData } from './ClaimProofExtractedData'
import { ClaimProofScoreBar } from './ClaimProofScoreBar'
import { ReviewerCommentBox } from './ReviewerCommentBox'
import type { ScreenshotVerificationStatus } from '../../../types/ClaimReviewTypes'

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
  verificationStatus?: ScreenshotVerificationStatus
}

interface ClaimProofGalleryProps {
  items: ClaimProofItem[]
  isAgency: boolean
  onApproveScreenshot?: (item: ClaimProofItem) => void
  onRejectScreenshot?: (item: ClaimProofItem, comment: string) => void
}

const ZOOM = 1 / LENS_FRACTION

const EXTRACTED_DATA_TOOLTIP =
  'Following details are extracted by our AI model from the screenshot(s) being displayed here.\nAI models can occasionally produce incorrect results, please review the details carefully before further action'

export function ClaimProofGallery({
  items,
  isAgency,
  onApproveScreenshot,
  onRejectScreenshot,
}: ClaimProofGalleryProps) {
  const [selectedIndex, setSelectedIndex] = useState(0)
  const [hovering, setHovering] = useState(false)
  const [pos, setPos] = useState({ x: 50, y: 50 })
  const [comment, setComment] = useState('')
  const [commentError, setCommentError] = useState('')

  function handleSelectScreenshot(index: number) {
    setSelectedIndex(index)
    setComment('')
    setCommentError('')
  }

  function handleRejectScreenshot() {
    if (!comment.trim()) {
      setCommentError('A comment is required when rejecting a screenshot.')
      return
    }
    setCommentError('')
    onRejectScreenshot?.(selected, comment)
  }

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
    <Card padded={false}>
      <div className="flex flex-col md:flex-row">
        <ClaimProofThumbnailRail
          items={items}
          selectedIndex={safeIndex}
          onSelect={handleSelectScreenshot}
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
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                  Extracted Data
                </h3>
                <div className="relative group flex-shrink-0">
                  <IconInfo size={14} className="text-ink-light-muted dark:text-ink-dark-muted cursor-help" />
                  <div className="absolute left-0 top-full mt-1.5 w-72 p-3 rounded-xl bg-surface-light-card dark:bg-surface-dark-card border border-surface-light-border dark:border-surface-dark-border shadow-lg text-xs text-ink-light-secondary dark:text-ink-dark-secondary leading-relaxed z-20 hidden group-hover:block whitespace-pre-line">
                    {EXTRACTED_DATA_TOOLTIP}
                  </div>
                </div>
              </div>
              <ClaimProofExtractedData fields={selected.fields} />
              <div className="mt-auto flex flex-col gap-3">
                <ClaimProofScoreBar score={score} />
                {isAgency && (
                  <>
                    <ReviewerCommentBox
                      value={comment}
                      onChange={v => { setComment(v); if (commentError) setCommentError('') }}
                      disabled={selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' || selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'}
                      error={commentError}
                    />
                    <div className="flex items-center gap-2 flex-wrap">
                      <Button
                        size="sm"
                        variant="secondary"
                        leftIcon={<IconCheck size={12} />}
                        onClick={() => onApproveScreenshot?.(selected)}
                        disabled={selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' || selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'}
                        className="!text-neon-green !border-neon-green/30 !bg-neon-green/10 hover:!bg-neon-green/20"
                      >
                        Approve
                      </Button>
                      <Button
                        size="sm"
                        variant="secondary"
                        leftIcon={<IconX size={12} />}
                        onClick={handleRejectScreenshot}
                        disabled={selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' || selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'}
                        className="!text-neon-red !border-neon-red/30 !bg-neon-red/10 hover:!bg-neon-red/20"
                      >
                        Reject
                      </Button>
                      {selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' && (
                        <span className="text-xs text-neon-green font-medium">Screenshot already verified!</span>
                      )}
                      {selected.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED' && (
                        <span className="text-xs text-neon-red font-medium">Screenshot rejected earlier!</span>
                      )}
                    </div>
                  </>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </Card>
  )
}
