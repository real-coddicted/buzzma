import { useState } from 'react'
import { Button } from '../Button'
import { ConfirmModal } from '../ConfirmModal'
import { IconCheck, IconX, IconCopyCheck } from '../icons'
import { ReviewerCommentBox } from './ReviewerCommentBox'

interface ClaimProofActionsProps {
  userRole: string | undefined
  isUnderReview: boolean
  mediatorVerified?: boolean
  onApprove: (comment: string) => void
  onVerified: () => void
  onReject: (comment: string) => void
}

export function ClaimProofActions({ userRole, isUnderReview, mediatorVerified, onApprove, onVerified, onReject }: ClaimProofActionsProps) {
  const [comment, setComment] = useState('')
  const [commentError, setCommentError] = useState('')
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

  return (
    <>
      <div className="space-y-3">
        {userRole === 'ROLE_AGENCY' && (
          <ReviewerCommentBox
            value={comment}
            onChange={v => { setComment(v); if (commentError) setCommentError('') }}
            disabled={!isUnderReview}
            error={commentError}
          />
        )}

        <div className="flex flex-wrap justify-end gap-2">
          {userRole === 'ROLE_AGENCY' && (
            <>
              <Button
                size="sm"
                variant="secondary"
                leftIcon={<IconCheck size={13} />}
                onClick={() => onApprove(comment)}
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
