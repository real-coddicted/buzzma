import { useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { submitReview, updateScreenshot } from '../../../api/claimApi'
import { useRejectedScreenshotUrl } from '../../../hooks/useRejectedScreenshotUrl'
import { ScreenshotPreview } from './ScreenshotPreview'
import { ScreenshotUpload } from './ScreenshotUpload'
import { inputClass, labelClass, submitBtnClass } from './claimStepStyles'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface ReviewStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function ReviewStep({ deal, claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: ReviewStepProps) {
  const [reviewUrl, setReviewUrl] = useState(claimResponse?.reviewUrl ?? '')
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const rejectedScreenshotUrl = useRejectedScreenshotUrl(rejectedScreenshot?.storageKey)
  const screenshotKey = rejectedScreenshot?.storageKey
    ?? claimResponse?.screenshots?.find(s => s.type === 'SCREENSHOT_TYPE_REVIEW')?.storageKey

  async function handleSubmit() {
    if (!claimId || !file) return
    setLoading(true)
    setError(null)
    try {
      const claim = rejectedScreenshot?.id
        ? await updateScreenshot(claimId, rejectedScreenshot.id, 'SCREENSHOT_TYPE_REVIEW', file, reviewUrl || undefined)
        : await submitReview(claimId, file, reviewUrl || undefined)
      onSuccess(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit review.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-5">
      {!readOnly && (
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
          Write a review for the product on {deal.platformLabel} and upload a screenshot of your
          published review.
        </p>
      )}
      <div>
        <label className={labelClass}>
          Review URL {!readOnly && <span className="text-neon-red">*</span>}
        </label>
        {readOnly ? (
          claimResponse?.reviewUrl ? (
            <a
              href={claimResponse.reviewUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-sm text-neon-blue hover:underline break-all"
            >
              {claimResponse.reviewUrl}
            </a>
          ) : (
            <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">—</p>
          )
        ) : (
          <input
            type="url"
            value={reviewUrl}
            onChange={e => setReviewUrl(e.target.value)}
            placeholder={`Paste your ${deal.platformLabel} review link`}
            className={inputClass}
          />
        )}
      </div>
      {readOnly && <ScreenshotPreview storageKey={screenshotKey} label="Review Screenshot" />}
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Review Screenshot"
            hint="Ensure your username and review text are clearly visible."
            onFileChange={setFile}
            initialPreview={rejectedScreenshotUrl ?? undefined}
          />
          {error && <p className="text-xs text-neon-red">{error}</p>}
          <button
            className={submitBtnClass('bg-neon-cyan hover:brightness-110')}
            onClick={handleSubmit}
            disabled={!file || !claimId || loading}
          >
            {loading ? 'Submitting…' : 'Submit Review'}
          </button>
        </>
      )}
    </div>
  )
}
