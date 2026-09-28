import { useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { createAppReviewClaim, updateScreenshot } from '../../../api/claimApi'
import { useRejectedScreenshotUrl } from '../../../hooks/useRejectedScreenshotUrl'
import { ScreenshotPreview } from './ScreenshotPreview'
import { ScreenshotUpload } from './ScreenshotUpload'
import { inputClass, labelClass, submitBtnClass } from './claimStepStyles'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface DownloadInstallStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function DownloadInstallStep({ deal, claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: DownloadInstallStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [accountName, setAccountName] = useState(claimResponse?.accountName ?? '')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const rejectedScreenshotUrl = useRejectedScreenshotUrl(rejectedScreenshot?.storageKey)

  const screenshotKey = rejectedScreenshot?.storageKey
    ?? claimResponse?.screenshots?.find(s => s.type === 'SCREENSHOT_TYPE_DOWNLOAD_INSTALL')?.storageKey

  async function handleSubmit() {
    if (!file || !accountName.trim()) return
    setLoading(true)
    setError(null)
    try {
      const claim = rejectedScreenshot?.id && claimId
        ? await updateScreenshot(claimId, rejectedScreenshot.id, 'SCREENSHOT_TYPE_DOWNLOAD_INSTALL', file)
        : await createAppReviewClaim({
            campaignId: deal.campaignId,
            dealId: deal.id,
            productName: deal.productName,
            accountName: accountName.trim(),
            screenshot: file,
          })
      onSuccess(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit claim.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-5">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        {readOnly
          ? 'Download & install proof submitted.'
          : `Download and install ${deal.productName} from ${deal.platformLabel}, then upload a screenshot showing it's installed.`}
      </p>

      {readOnly && (
        <div>
          <label className={labelClass}>Account Name</label>
          <input className={inputClass} value={accountName} disabled readOnly />
        </div>
      )}
      {!readOnly && (
        <div>
          <label className={labelClass}>Account Name *</label>
          <input
            className={inputClass}
            placeholder="Name or email shown on the app store"
            value={accountName}
            onChange={e => setAccountName(e.target.value)}
          />
        </div>
      )}

      {readOnly
        ? <ScreenshotPreview storageKey={screenshotKey} label="Download & Install Screenshot" />
        : (
          <ScreenshotUpload
            label="Download & Install Screenshot"
            hint="Show the app installed on your device, or the 'Installed'/'Open' button on the store listing."
            onFileChange={setFile}
            initialPreview={rejectedScreenshotUrl ?? undefined}
          />
        )}

      {!readOnly && (
        <>
          {error && <p className="text-xs text-neon-red">{error}</p>}
          <button
            className={submitBtnClass('bg-neon-blue hover:brightness-110')}
            onClick={handleSubmit}
            disabled={!file || !accountName.trim() || loading}
          >
            {loading ? 'Submitting…' : 'Submit'}
          </button>
        </>
      )}
    </div>
  )
}
