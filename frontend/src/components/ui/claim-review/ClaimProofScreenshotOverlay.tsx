import { useEffect, useState } from 'react'
import { Button } from '../Button'
import { IconCheck, IconX } from '../icons'
import { ClaimProofScoreBar } from './ClaimProofScoreBar'
import { ClaimProofCompareTable } from './ClaimProofCompareTable'
import { ReviewerCommentBox } from './ReviewerCommentBox'
import { SCREENSHOT_TYPE_CONFIG } from './claimReviewConstants'
import type { ClaimProofItem } from './ClaimProofGallery'

interface Props {
  item: ClaimProofItem
  idx: number
  score: number
  isAgency: boolean
  hasPrev: boolean
  hasNext: boolean
  onPrev: () => void
  onNext: () => void
  onClose: () => void
  onApprove: () => void
  onReject: (comment: string) => void
}

export function ClaimProofScreenshotOverlay({ item, idx, score, isAgency, hasPrev, hasNext, onPrev, onNext, onClose, onApprove, onReject }: Props) {
  const [fullImage, setFullImage] = useState(false)
  const [comment, setComment] = useState('')

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
      else if (e.key === 'ArrowLeft' && hasPrev) onPrev()
      else if (e.key === 'ArrowRight' && hasNext) onNext()
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose, hasPrev, hasNext, onPrev, onNext])
  const [commentError, setCommentError] = useState('')

  // Reset comment when navigating between screenshots
  useEffect(() => { setComment(''); setCommentError('') }, [item.id])

  const isActioned =
    item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' ||
    item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'

  function handleReject() {
    if (!comment.trim()) {
      setCommentError('A comment is required when rejecting a screenshot.')
      return
    }
    setCommentError('')
    onReject(comment)
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div
        className="flex flex-col bg-surface-light-base dark:bg-surface-dark-base rounded-2xl overflow-hidden shadow-2xl border border-surface-light-border dark:border-surface-dark-border"
        style={{ width: '95vw', maxHeight: '88vh' }}
      >
        {/* header */}
        <div className="flex items-center gap-3 px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0">
          <span className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary flex-1">
            {SCREENSHOT_TYPE_CONFIG[item.type ?? '']?.label ?? `Screenshot ${idx + 1}`}
          </span>
          <div className="flex items-center gap-1">
            <button
              onClick={onPrev}
              disabled={!hasPrev}
              title="Previous screenshot (←)"
              className="inline-flex items-center justify-center w-7 h-7 rounded-lg text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
            >
              ‹
            </button>
            <button
              onClick={onNext}
              disabled={!hasNext}
              title="Next screenshot (→)"
              className="inline-flex items-center justify-center w-7 h-7 rounded-lg text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
            >
              ›
            </button>
          </div>
          <button
            onClick={() => setFullImage(f => !f)}
            className="text-xs px-2.5 py-1 rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
          >
            {fullImage ? 'Show comparison' : '⤢ Full image'}
          </button>
          <button
            onClick={onClose}
            className="inline-flex items-center justify-center w-7 h-7 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
          >
            <IconX size={14} />
          </button>
        </div>

        {/* body */}
        <div className="flex flex-1 min-h-0">
          {/* image panel — flex:3 ≈ 60% */}
          <div className={[
            'flex-shrink-0 overflow-auto bg-surface-light-hover dark:bg-surface-dark-hover transition-all duration-200',
            fullImage ? 'flex-1' : 'basis-[60%]',
          ].join(' ')}>
            <img
              src={item.imageUrl}
              alt={item.imageAlt ?? 'Screenshot'}
              className="w-full h-auto"
            />
          </div>

          {/* table panel — flex:2 ≈ 40%, hidden in full-image mode */}
          {!fullImage && (
            <div className="flex flex-col basis-[40%] min-w-[260px] border-l border-surface-light-border dark:border-surface-dark-border overflow-hidden">
              <div className="px-3 pt-3 pb-2 flex-shrink-0">
                <ClaimProofScoreBar score={score} />
              </div>
              <div className="flex-1 overflow-y-auto">
                <ClaimProofCompareTable fields={item.fields} />
              </div>
            </div>
          )}
        </div>

        {/* footer — agency actions */}
        {isAgency && (
          <div className="flex-shrink-0 border-t border-surface-light-border dark:border-surface-dark-border px-4 py-3 space-y-2">
            {isActioned ? (
              <div className="flex justify-end">
                <span className={[
                  'text-xs font-semibold',
                  item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' ? 'text-neon-green' : 'text-neon-red',
                ].join(' ')}>
                  {item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' ? '✓ Already verified' : '✗ Already rejected'}
                </span>
              </div>
            ) : (
              <>
                <ReviewerCommentBox
                  value={comment}
                  onChange={v => { setComment(v); if (commentError) setCommentError('') }}
                  error={commentError}
                />
                <div className="flex flex-wrap items-center justify-end gap-2">
                  <Button
                    size="sm"
                    variant="secondary"
                    leftIcon={<IconCheck size={12} />}
                    onClick={onApprove}
                    className="!text-neon-green !border-neon-green/30 !bg-neon-green/10 hover:!bg-neon-green/20"
                  >
                    Approve
                  </Button>
                  <Button
                    size="sm"
                    variant="secondary"
                    leftIcon={<IconX size={12} />}
                    onClick={handleReject}
                    className="!text-neon-red !border-neon-red/30 !bg-neon-red/10 hover:!bg-neon-red/20"
                  >
                    Reject
                  </Button>
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
