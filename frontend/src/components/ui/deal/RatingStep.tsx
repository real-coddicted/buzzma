import { useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { submitRating, updateScreenshot } from '../../../api/claimApi'
import { useRejectedScreenshotUrl } from '../../../hooks/useRejectedScreenshotUrl'
import { ScreenshotPreview } from './ScreenshotPreview'
import { ScreenshotUpload } from './ScreenshotUpload'
import { submitBtnClass } from './claimStepStyles'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface RatingStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function RatingStep({ deal, claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: RatingStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const rejectedScreenshotUrl = useRejectedScreenshotUrl(rejectedScreenshot?.storageKey)

  const screenshotKey = rejectedScreenshot?.storageKey
    ?? claimResponse?.screenshots?.find(s => s.type === 'SCREENSHOT_TYPE_RATING')?.storageKey

  async function handleSubmit() {
    if (!claimId || !file) return
    setLoading(true)
    setError(null)
    try {
      const claim = rejectedScreenshot?.id
        ? await updateScreenshot(claimId, rejectedScreenshot.id, 'SCREENSHOT_TYPE_RATING', file)
        : await submitRating(claimId, file)
      onSuccess(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit rating.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-5">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        {readOnly
          ? 'Rating submitted.'
          : `Rate the product on ${deal.platformLabel} and upload a screenshot of your submitted rating.`}
      </p>
      {readOnly && <ScreenshotPreview storageKey={screenshotKey} label="Rating Screenshot" />}
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Rating Screenshot"
            hint="Show the star rating you submitted on the product page."
            onFileChange={setFile}
            initialPreview={rejectedScreenshotUrl ?? undefined}
          />
          {error && <p className="text-xs text-neon-red">{error}</p>}
          <button
            className={submitBtnClass('bg-neon-purple hover:brightness-110')}
            onClick={handleSubmit}
            disabled={!file || !claimId || loading}
          >
            {loading ? 'Submitting…' : 'Submit Rating'}
          </button>
        </>
      )}
    </div>
  )
}
