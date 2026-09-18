import { useState } from 'react'
import type { components } from '../../../types/api'
import { submitSellerFeedback, updateScreenshot } from '../../../api/claimApi'
import { STEP_TYPE_TO_SCREENSHOT_TYPE } from '../../../constants/claimSteps'
import { useRejectedScreenshotUrl } from '../../../hooks/useRejectedScreenshotUrl'
import { ScreenshotPreview } from './ScreenshotPreview'
import { ScreenshotUpload } from './ScreenshotUpload'
import { submitBtnClass } from './claimStepStyles'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface SellerFeedbackStepProps {
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function SellerFeedbackStep({ claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: SellerFeedbackStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const rejectedScreenshotUrl = useRejectedScreenshotUrl(rejectedScreenshot?.storageKey)
  // 'SCREENSHOT_TYPE_SELLER_FEEDBACK' isn't in the generated ClaimScreenshotResponseDto['type']
  // union yet (types/api.ts is stale until regenerated against a running backend), so look it up
  // via the string-typed map instead of comparing against the literal directly.
  const screenshotKey = rejectedScreenshot?.storageKey
    ?? claimResponse?.screenshots?.find(s => s.type === STEP_TYPE_TO_SCREENSHOT_TYPE.SELLER_FEEDBACK)?.storageKey

  async function handleSubmit() {
    if (!claimId || !file) return
    setLoading(true)
    setError(null)
    try {
      const claim = rejectedScreenshot?.id
        ? await updateScreenshot(claimId, rejectedScreenshot.id, 'SCREENSHOT_TYPE_SELLER_FEEDBACK', file)
        : await submitSellerFeedback(claimId, file)
      onSuccess(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit seller feedback screenshot.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-5">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        {readOnly
          ? 'Seller feedback screenshot submitted.'
          : 'Leave feedback for the seller and upload a screenshot of your submitted feedback.'}
      </p>
      {readOnly && <ScreenshotPreview storageKey={screenshotKey} label="Seller Feedback Screenshot" />}
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Seller Feedback Screenshot"
            hint="Ensure the seller name, your rating, and any comment text are clearly visible."
            onFileChange={setFile}
            initialPreview={rejectedScreenshotUrl ?? undefined}
          />
          {error && <p className="text-xs text-neon-red">{error}</p>}
          <button
            className={submitBtnClass('bg-neon-cyan hover:brightness-110')}
            onClick={handleSubmit}
            disabled={!file || !claimId || loading}
          >
            {loading ? 'Submitting…' : 'Submit Screenshot'}
          </button>
        </>
      )}
    </div>
  )
}
