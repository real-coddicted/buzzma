import { useState } from 'react'
import { Button } from '../Button'
import { ConfirmModal } from '../ConfirmModal'
import { IconCheck, IconX, IconCopyCheck, IconEdit } from '../icons'
import { ReviewerCommentBox } from './ReviewerCommentBox'
import { RupeeInput } from '../RupeeInput'
import { paiseToRupees } from '../../../utils/currency'
import { canReviewClaims, canApproveClaims, canResetClaim } from './claimUtils'
import type { ClaimStatus } from '../../../types'

interface ClaimProofActionsProps {
  userRole: string | undefined
  claimStatus: ClaimStatus
  isUnderReview: boolean
  mediatorVerified?: boolean
  brandVerified?: boolean
  initialAmountApprovedPaise?: number
  onApprove: (comment: string, amountApprovedPaise?: number) => void
  onVerified: () => void
  onBrandVerified: () => void
  onReject: (comment: string) => void
  onReset: () => void
}

export function ClaimProofActions({ userRole, claimStatus, isUnderReview, mediatorVerified, brandVerified, initialAmountApprovedPaise, onApprove, onVerified, onBrandVerified, onReject, onReset }: ClaimProofActionsProps) {
  const [comment, setComment] = useState('')
  const [commentError, setCommentError] = useState('')
  const [approvedAmountRupees, setApprovedAmountRupees] = useState(
    initialAmountApprovedPaise != null ? paiseToRupees(initialAmountApprovedPaise).toFixed(2) : ''
  )
  const [amountError, setAmountError] = useState('')
  const [showRejectConfirm, setShowRejectConfirm] = useState(false)
  const [showResetConfirm, setShowResetConfirm] = useState(false)

  if (!canReviewClaims(userRole) && userRole !== 'ROLE_MEDIATOR') return null

  const canReset = canResetClaim(userRole, claimStatus)

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

  function handleResetConfirm() {
    setShowResetConfirm(false)
    onReset()
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
        {canApproveClaims(userRole) && (
          <ReviewerCommentBox
            value={comment}
            onChange={v => { setComment(v); if (commentError) setCommentError('') }}
            disabled={!isUnderReview}
            error={commentError}
          />
        )}

        {canApproveClaims(userRole) && (
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
        )}

        <div className="flex flex-wrap justify-end gap-2">
          {canApproveClaims(userRole) && (
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
          )}
          {userRole === 'ROLE_BRAND' && (
            <Button
              size="sm"
              variant="secondary"
              leftIcon={<IconCopyCheck size={13} />}
              onClick={onBrandVerified}
              disabled={!isUnderReview || brandVerified}
              className={brandVerified
                ? "!text-neon-green !border-neon-green/30 !bg-neon-green/10"
                : "!text-neon-blue !border-neon-blue/30 !bg-neon-blue/10 hover:!bg-neon-blue/20"}
            >
              Verified
            </Button>
          )}
          {canApproveClaims(userRole) && (
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
          {canReset && (
            <Button
              size="sm"
              variant="secondary"
              leftIcon={<IconEdit size={13} />}
              onClick={() => setShowResetConfirm(true)}
              className="!text-neon-blue !border-neon-blue/30 !bg-neon-blue/10 hover:!bg-neon-blue/20"
            >
              Reset Review
            </Button>
          )}
        </div>
      </div>

      {showRejectConfirm && (
        <ConfirmModal
          title="Reject claim"
          message="This cannot be undone — once rejected, the claim cannot be reopened. Are you sure you want to reject this claim?"
          confirmLabel="Yes, reject"
          cancelLabel="No"
          tone="red"
          onConfirm={handleRejectConfirm}
          onCancel={() => setShowRejectConfirm(false)}
        />
      )}

      {showResetConfirm && (
        <ConfirmModal
          title={userRole === 'ROLE_BRAND' ? 'Reset your verification' : 'Reset review'}
          message={
            userRole === 'ROLE_BRAND'
              ? 'This clears your verification sign-off so you can re-review the claim. Note that screenshot-level review may still be locked — contact your agency if you need those reopened too.'
              : 'This reopens the claim back to Under Review, so Approve/Reject and screenshot review become available again. Are you sure?'
          }
          confirmLabel="Yes, reset"
          cancelLabel="No"
          tone="blue"
          onConfirm={handleResetConfirm}
          onCancel={() => setShowResetConfirm(false)}
        />
      )}
    </>
  )
}
