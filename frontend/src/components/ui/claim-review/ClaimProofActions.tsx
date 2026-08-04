import { useState } from 'react'
import { Button } from '../Button'
import { ConfirmModal } from '../ConfirmModal'
import { IconCheck, IconX, IconCopyCheck } from '../icons'
import { ReviewerCommentBox } from './ReviewerCommentBox'
import { RupeeInput } from '../RupeeInput'
import { paiseToRupees } from '../../../utils/currency'

interface ClaimProofActionsProps {
  userRole: string | undefined
  isUnderReview: boolean
  mediatorVerified?: boolean
  initialAmountApprovedPaise?: number
  onApprove: (comment: string, amountApprovedPaise?: number) => void
  onVerified: () => void
  onReject: (comment: string) => void
}

export function ClaimProofActions({ userRole, isUnderReview, mediatorVerified, initialAmountApprovedPaise, onApprove, onVerified, onReject }: ClaimProofActionsProps) {
  const [comment, setComment] = useState('')
  const [commentError, setCommentError] = useState('')
  const [approvedAmountRupees, setApprovedAmountRupees] = useState(
    initialAmountApprovedPaise != null ? paiseToRupees(initialAmountApprovedPaise).toFixed(2) : ''
  )
  const [amountError, setAmountError] = useState('')
  const [showRejectConfirm, setShowRejectConfirm] = useState(false)

  if (userRole !== 'ROLE_AGENCY' && userRole !== 'ROLE_MEDIATOR') return null

  function handleRejectClick() {
    if (!comment.trim()) {
      setCommentError('A comment is required when rejecting a claim.')
      return
    }
    setCommentError('')
    setShowRejectConfirm(true)
  }

  function handleRejectConfirm() {
    setShowRejectConfirm(false)
    onReject(comment)
  }

  function handleApproveClick() {
    if (!approvedAmountRupees.trim()) {
      setAmountError('Approved amount is mandatory to approve a claim.')
      return
    }
    setAmountError('')
    const paise = Math.round(parseFloat(approvedAmountRupees) * 100)
    onApprove(comment, paise)
  }

  return (
    <>
      <div className="space-y-3">
        {userRole === 'ROLE_AGENCY' && (
          <>
            <ReviewerCommentBox
              value={comment}
              onChange={v => { setComment(v); if (commentError) setCommentError('') }}
              disabled={!isUnderReview}
              error={commentError}
            />
            <div className="flex flex-col gap-1">
              <label className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
                Approved Amount <span className="text-neon-red">*</span>
              </label>
              <RupeeInput
                value={approvedAmountRupees}
                onChange={v => { setApprovedAmountRupees(v); if (amountError) setAmountError('') }}
                placeholder="499.00"
                disabled={!isUnderReview}
                className="w-full rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-raised dark:bg-surface-dark-raised text-xs text-ink-light-primary dark:text-ink-dark-primary pr-3 py-2 focus:outline-none focus:border-neon-blue/40 disabled:opacity-50"
              />
              {amountError && <p className="text-xs text-neon-red">{amountError}</p>}
            </div>
          </>
        )}

        <div className="flex flex-wrap justify-end gap-2">
          {userRole === 'ROLE_AGENCY' && (
            <>
              <Button
                size="sm"
                variant="secondary"
                leftIcon={<IconCheck size={13} />}
                onClick={handleApproveClick}
                disabled={!isUnderReview}
                className="!text-neon-green !border-neon-green/30 !bg-neon-green/10 hover:!bg-neon-green/20"
              >
                Approve claim
              </Button>
              <Button
                size="sm"
                variant="secondary"
                leftIcon={<IconX size={13} />}
                onClick={handleRejectClick}
                disabled={!isUnderReview}
                className="!text-neon-red !border-neon-red/30 !bg-neon-red/10 hover:!bg-neon-red/20"
              >
                Reject claim
              </Button>
            </>
          )}
          {userRole === 'ROLE_MEDIATOR' && (
            <Button
              size="sm"
              variant="secondary"
              leftIcon={<IconCopyCheck size={13} />}
              onClick={onVerified}
              disabled={!isUnderReview || mediatorVerified}
              className={mediatorVerified
                ? "!text-neon-green !border-neon-green/30 !bg-neon-green/10"
                : "!text-neon-blue !border-neon-blue/30 !bg-neon-blue/10 hover:!bg-neon-blue/20"}
            >
              Verified
            </Button>
          )}
        </div>
      </div>

      {showRejectConfirm && (
        <ConfirmModal
          title="Reject claim"
          message="Are you sure you want to reject this claim?"
          confirmLabel="Yes"
          cancelLabel="No"
          tone="red"
          onConfirm={handleRejectConfirm}
          onCancel={() => setShowRejectConfirm(false)}
        />
      )}
    </>
  )
}
