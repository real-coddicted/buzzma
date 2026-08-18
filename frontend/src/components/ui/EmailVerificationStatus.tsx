import { useState } from 'react'
import { VerifyEmailModal } from './VerifyEmailModal'

interface EmailVerificationStatusProps {
  email: string
  verified: boolean
  onVerified?: () => void
}

export function EmailVerificationStatus({ email, verified, onVerified }: EmailVerificationStatusProps) {
  const [modalOpen, setModalOpen] = useState(false)

  return (
    <>
      {verified ? (
        <span className="text-xs font-medium px-2 py-0.5 rounded-full border border-neon-green/30 bg-neon-green/10 text-neon-green">
          Verified
        </span>
      ) : (
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium px-2 py-0.5 rounded-full border border-neon-yellow/30 bg-neon-yellow/10 text-neon-yellow">
            Not verified
          </span>
          <button
            type="button"
            onClick={() => setModalOpen(true)}
            className="text-xs font-medium text-neon-blue hover:underline"
          >
            Verify
          </button>
        </div>
      )}

      {modalOpen && (
        <VerifyEmailModal
          email={email}
          onClose={() => setModalOpen(false)}
          onVerified={() => onVerified?.()}
        />
      )}
    </>
  )
}