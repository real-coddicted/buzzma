import { useEffect, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import type { CampaignStepDto } from '../../../api/campaignApi'
import { fetchStepConfig } from '../../../api/campaignApi'
import { STEP_TYPE_COLORS, STEP_TYPE_TO_SCREENSHOT_TYPE } from '../../../constants/claimSteps'
import { paiseToRupees } from '../../../utils/currency'
import { submitRating, submitReview, submitReturn, updateScreenshot } from '../../../api/claimApi'
import { DealOrderForm } from './DealOrderForm'
import { ScreenshotUpload } from './ScreenshotUpload'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

const inputClass = [
  'w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border',
  'bg-surface-light-hover dark:bg-surface-dark-hover',
  'text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'px-3 py-2 outline-none focus:border-neon-blue/50 transition-colors',
].join(' ')

const labelClass = 'block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5'

function submitBtnClass(color: string) {
  return `w-full py-2.5 rounded-lg text-surface-dark-base text-sm font-semibold transition-all disabled:opacity-40 disabled:cursor-not-allowed ${color}`
}

interface OrderStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

function OrderStep({ deal, claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: OrderStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleResubmit() {
    if (!claimId || !file || !rejectedScreenshot?.id) return
    setLoading(true)
    setError(null)
    try {
      const claim = await updateScreenshot(claimId, rejectedScreenshot.id, 'SCREENSHOT_TYPE_ORDER', file)
      onSuccess(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update screenshot.')
    } finally {
      setLoading(false)
    }
  }

  if (rejectedScreenshot) {
    return (
      <div className="space-y-5">
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
          Your order screenshot was rejected. Upload a new one below.
        </p>
        <ScreenshotUpload
          label="Order Screenshot"
          hint="Show your order confirmation page clearly."
          onFileChange={setFile}
        />
        {error && <p className="text-xs text-neon-red">{error}</p>}
        <button
          className={submitBtnClass('bg-neon-blue hover:brightness-110')}
          onClick={handleResubmit}
          disabled={!file || loading}
        >
          {loading ? 'Submitting…' : 'Resubmit Screenshot'}
        </button>
      </div>
    )
  }

  const claimValues = readOnly && claimResponse ? {
    orderId:     claimResponse.ecommerceOrderId ?? '',
    amount:      claimResponse.amountPaise != null ? String(paiseToRupees(claimResponse.amountPaise)) : '',
    productName: claimResponse.productName ?? '',
    sellerName:  claimResponse.sellerName ?? '',
    orderDate:   claimResponse.orderDate != null
      ? String(claimResponse.orderDate).replace(/^(\d{4})(\d{2})(\d{2})$/, '$1-$2-$3')
      : '',
    accountName: claimResponse.accountName ?? '',
  } : undefined
  return (
    <div className="space-y-4">
      {!readOnly && (
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
          Purchase this product on{' '}
          <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            {deal.platformLabel}
          </span>{' '}
          at the offered price, then upload the screenshot of your order confirmation and fill in
          your order details below.
        </p>
      )}
      <DealOrderForm
        dealId={deal.id}
        campaignId={deal.campaignId}
        onSuccess={onSuccess}
        readOnly={readOnly}
        claimValues={claimValues}
      />
    </div>
  )
}

interface RatingStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

function RatingStep({ deal, claimId, onSuccess, readOnly = false, rejectedScreenshot }: RatingStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Rating Screenshot"
            hint="Show the star rating you submitted on the product page."
            onFileChange={setFile}
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

interface ReviewStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

function ReviewStep({ deal, claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: ReviewStepProps) {
  const [reviewUrl, setReviewUrl] = useState('')
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit() {
    if (!claimId || !file) return
    setLoading(true)
    setError(null)
    try {
      const claim = rejectedScreenshot?.id
        ? await updateScreenshot(claimId, rejectedScreenshot.id, 'SCREENSHOT_TYPE_REVIEW', file)
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
          Review URL {!readOnly && !rejectedScreenshot && <span className="text-neon-red">*</span>}
        </label>
        {readOnly || rejectedScreenshot ? (
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
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Review Screenshot"
            hint="Ensure your username and review text are clearly visible."
            onFileChange={setFile}
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

interface ReturnStepProps {
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

function ReturnStep({ claimId, onSuccess, readOnly = false, rejectedScreenshot }: ReturnStepProps) {
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Return Window Completed Screenshot"
            hint="Ensure the order ID and product name are clearly visible."
            onFileChange={setFile}
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

function CashbackStep() {
  return (
    <div className="space-y-4">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        Your submission is under review. Cashback will be credited to your account within{' '}
        <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          7–14 business days
        </span>{' '}
        after verification.
      </p>
      <div className="rounded-xl border border-neon-green/20 bg-neon-green/5 px-4 py-4 space-y-1">
        <p className="text-xs font-semibold text-neon-green">Pending Verification</p>
        <p className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
          No further action required. You will be notified once cashback is processed.
        </p>
      </div>
    </div>
  )
}

interface ClaimStepFormProps {
  deal: Deal
  currentStep: number
  onStepChange: (step: number) => void
  onClaimUpdate?: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
}


export function ClaimStepForm({ deal, currentStep, onStepChange, onClaimUpdate, readOnly = false, claimResponse }: ClaimStepFormProps) {
  const [steps, setSteps] = useState<CampaignStepDto[]>([])
  const [localClaim, setLocalClaim] = useState<ClaimResponseDto | undefined>(undefined)

  useEffect(() => {
    fetchStepConfig().then(config => {
      setSteps(config[deal.dealType] ?? [])
    })
  }, [deal.dealType])

  const step = steps[currentStep]
  const stepType = step?.type ?? ''
  const stepColor = STEP_TYPE_COLORS[stepType]?.color ?? 'text-neon-blue'
  const effectiveClaim = localClaim ?? claimResponse

  const screenshotType = STEP_TYPE_TO_SCREENSHOT_TYPE[stepType]
  const rejectedScreenshot = screenshotType
    ? (effectiveClaim?.screenshots ?? []).find(
        s => s.type === screenshotType && s.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED'
      )
    : undefined

  function handleClaimSuccess(claim: ClaimResponseDto) {
    setLocalClaim(claim)
    onClaimUpdate?.(claim)
    onStepChange(claim.currentStep ?? currentStep + 1)
  }

  return (
    <div className="space-y-5">
      <div>
        <h3 className="text-base font-bold text-ink-light-primary dark:text-ink-dark-primary">
          {step?.label ?? ''}
        </h3>
        <p className={['text-[10px] font-semibold uppercase tracking-wider mt-0.5', stepColor].join(' ')}>
          Step {currentStep + 1} of {steps.length}
        </p>
      </div>

      {stepType === 'ORDER'         && <OrderStep  deal={deal} claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'RATING'        && <RatingStep deal={deal} claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'REVIEW'        && <ReviewStep deal={deal} claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'RETURN_WINDOW' && <ReturnStep claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'CASHBACK'      && <CashbackStep />}
    </div>
  )
}
