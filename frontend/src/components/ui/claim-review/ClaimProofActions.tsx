import { Button } from '../Button'
import { IconCheck, IconX, IconCopyCheck } from '../icons'

interface ClaimProofActionsProps {
  userRole: string | undefined
  isUnderReview: boolean
  onApprove: () => void
  onVerified: () => void
  onReject: () => void
}

export function ClaimProofActions({ userRole, isUnderReview, onApprove, onVerified, onReject }: ClaimProofActionsProps) {
  if (userRole !== 'ROLE_AGENCY' && userRole !== 'ROLE_MEDIATOR') return null

  return (
    <div className="flex flex-wrap justify-end gap-2">
      {userRole === 'ROLE_AGENCY' && (
        <>
          <Button
            size="sm"
            variant="secondary"
            leftIcon={<IconCheck size={13} />}
            onClick={onApprove}
            disabled={!isUnderReview}
            className="!text-neon-green !border-neon-green/30 !bg-neon-green/10 hover:!bg-neon-green/20"
          >
            Approve claim
          </Button>
          <Button
            size="sm"
            variant="secondary"
            leftIcon={<IconX size={13} />}
            onClick={onReject}
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
          disabled={!isUnderReview}
          className="!text-neon-blue !border-neon-blue/30 !bg-neon-blue/10 hover:!bg-neon-blue/20"
        >
          Verified
        </Button>
      )}
    </div>
  )
}
