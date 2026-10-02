import { useState } from 'react'
import type { components } from '../../../types/api'
import { updateScreenshot } from '../../../api/claimApi'
import { useRejectedScreenshotUrl } from '../../../hooks/useRejectedScreenshotUrl'
import { ScreenshotPreview } from './ScreenshotPreview'
import { ScreenshotUpload } from './ScreenshotUpload'
import { submitBtnClass } from './claimStepStyles'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface ScreenshotStepProps {
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
  screenshotType: string
  label: string
  description: string
  hint: string
  buttonLabel: string
  buttonColor: string
  onSubmit: (claimId: string, file: File) => Promise<ClaimResponseDto>
  onCreate?: (file: File) => Promise<ClaimResponseDto>
}

export function ScreenshotStep({
  claimId,
  onSuccess,
  readOnly = false,
  claimResponse,
  rejectedScreenshot,
  screenshotType,
  label,
  description,
  hint,
  buttonLabel,
  buttonColor,
  onSubmit,
  onCreate,
}: ScreenshotStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const rejectedScreenshotUrl = useRejectedScreenshotUrl(rejectedScreenshot?.storageKey)

  const screenshotKey = rejectedScreenshot?.storageKey
    ?? claimResponse?.screenshots?.find(s => s.type === screenshotType)?.storageKey

  const canSubmit = file && !loading && (claimId || onCreate)

  async function handleSubmit() {
    if (!file) return
    setLoading(true)
    setError(null)
    try {
      let claim: ClaimResponseDto
      if (rejectedScreenshot?.id && claimId) {
        claim = await updateScreenshot(claimId, rejectedScreenshot.id, screenshotType, file)
      } else if (claimId) {
        claim = await onSubmit(claimId, file)
      } else if (onCreate) {
        claim = await onCreate(file)
      } else {
        return
      }
      onSuccess(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to submit ${label.toLowerCase()}.`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-5">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        {readOnly ? `${label} submitted.` : description}
      </p>
      {readOnly && <ScreenshotPreview storageKey={screenshotKey} label={`${label} Screenshot`} />}
      {!readOnly && (
        <>
          <ScreenshotUpload
            label={`${label} Screenshot`}
            hint={hint}
            onFileChange={setFile}
            initialPreview={rejectedScreenshotUrl ?? undefined}
          />
          {error && <p className="text-xs text-neon-red">{error}</p>}
          <button
            className={submitBtnClass(buttonColor)}
            onClick={handleSubmit}
            disabled={!canSubmit}
          >
            {loading ? 'Submitting...' : buttonLabel}
          </button>
        </>
      )}
    </div>
  )
}
