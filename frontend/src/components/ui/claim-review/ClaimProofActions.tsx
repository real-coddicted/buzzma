import { Button } from '../Button'
import { IconCheck, IconX, IconEye, IconCopyCheck } from '../icons'

interface ClaimProofActionsProps {
  onApprove: () => void
  onRequestProof: () => void
  onVerified: () => void
  onReject: () => void
}

export function ClaimProofActions({
  onApprove,
  onRequestProof,
  onVerified,
  onReject,
}: ClaimProofActionsProps) {
  return (
    <div className="flex flex-wrap justify-end gap-2">
      <Button
        size="sm"
        variant="secondary"
        leftIcon={<IconCheck size={13} />}
        onClick={onApprove}
        className="!text-neon-green !border-neon-green/30 !bg-neon-green/10 hover:!bg-neon-green/20"
      >
        Approve
      </Button>
      <Button
        size="sm"
        variant="secondary"
        leftIcon={<IconEye size={13} />}
        onClick={onRequestProof}
        className="!text-neon-orange !border-neon-orange/30 !bg-neon-orange/10 hover:!bg-neon-orange/20"
      >
        Request Proof
      </Button>
      <Button
        size="sm"
        variant="secondary"
        leftIcon={<IconCopyCheck size={13} />}
        onClick={onVerified}
        className="!text-neon-blue !border-neon-blue/30 !bg-neon-blue/10 hover:!bg-neon-blue/20"
      >
        Verified
      </Button>
      <Button
        size="sm"
        variant="secondary"
        leftIcon={<IconX size={13} />}
        onClick={onReject}
        className="!text-neon-red !border-neon-red/30 !bg-neon-red/10 hover:!bg-neon-red/20"
      >
        Reject
      </Button>
    </div>
  )
}
