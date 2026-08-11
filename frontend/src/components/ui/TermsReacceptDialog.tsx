import { useState } from 'react'
import { Modal } from './Modal'
import { Button } from './Button'
import { TermsLink } from './TermsLink'
import { Toast } from './Toast'
import { acceptTerms } from '../../api/termsApi'

interface TermsReacceptDialogProps {
  onAccepted: () => void
}

export function TermsReacceptDialog({ onAccepted }: TermsReacceptDialogProps) {
  const [accepting, setAccepting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleAccept() {
    setAccepting(true)
    setError(null)
    try {
      await acceptTerms()
      onAccepted()
    } catch {
      setError('Something went wrong. Please try again.')
    } finally {
      setAccepting(false)
    }
  }

  return (
    <>
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
      <Modal>
        <h2 className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          Terms and Conditions Updated
        </h2>
        <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
          Please review and accept the updated <TermsLink /> to continue.
        </p>
        <Button variant="green" size="lg" loading={accepting} onClick={handleAccept} className="w-full">
          I Accept
        </Button>
      </Modal>
    </>
  )
}