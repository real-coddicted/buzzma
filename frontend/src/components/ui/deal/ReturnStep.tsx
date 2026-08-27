import { useState } from 'react'
import type { components } from '../../../types/api'
import { submitReturn, updateScreenshot } from '../../../api/claimApi'
import { useRejectedScreenshotUrl } from '../../../hooks/useRejectedScreenshotUrl'
import { ScreenshotPreview } from './ScreenshotPreview'
import { ScreenshotUpload } from './ScreenshotUpload'
import { submitBtnClass } from './claimStepStyles'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface ReturnStepProps {
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function ReturnStep({ claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: ReturnStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const rejectedScreenshotUrl = useRejectedScreenshotUrl(rejectedScreenshot?.storageKey)
  const screenshotKey = rejectedScreenshot?.storageKey
    ?? claimResponse?.screenshots?.find(s => s.type === 'SCREENSHOT_TYPE_RETURN')?.storageKey

  async function handleSubmit() {
    if (!claimId || !file) return
    setLoading(true)
    setError(null)
    try {
      const claim = rejectedScreenshot?.id
        ? await updateScreenshot(claimId, rejectedScreenshot.id, 'SCREENSHOT_TYPE_RETURN', file)
        : await submitReturn(claimId, file)
      onSuccess(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit return screenshot.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-5">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        {readOnly
          ? 'Return window screenshot submitted.'
          : 'Upload a screenshot of return window completed page.'}
      </p>
      {readOnly && <ScreenshotPreview storageKey={screenshotKey} label="Return Window Screenshot" />}
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Return Window Completed Screenshot"
            hint="Ensure the order ID and product name are clearly visible."
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
