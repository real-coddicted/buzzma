import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { CLAIM_STEPS } from '../../../constants/claimSteps'
import { DealOrderForm } from './DealOrderForm'
import { ScreenshotUpload } from './ScreenshotUpload'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']

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
  onSuccess: (claim: ClaimResponseDto) => void
}

function OrderStep({ deal, onSuccess }: OrderStepProps) {
  return (
    <div className="space-y-4">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        Purchase this product on{' '}
        <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          {deal.platformLabel}
        </span>{' '}
        at the offered price, then upload the screenshot of your order confirmation and fill in
        your order details below.
      </p>
      <DealOrderForm
        dealId={deal.id}
        campaignId={deal.campaignId}
        onSuccess={onSuccess}
      />
    </div>
  )
}

function ReviewStep({ deal }: { deal: Deal }) {
  const isRating = deal.dealType === 'CAMPAIGN_TYPE_RATING'

  return (
    <div className="space-y-5">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        {isRating
          ? `Rate the product on ${deal.platformLabel} and upload a screenshot of your submitted rating.`
          : `Write a review for the product on ${deal.platformLabel} and upload a screenshot of your published review.`}
      </p>

      {!isRating && (
        <div>
          <label className={labelClass}>
            Review URL <span className="text-neon-red">*</span>
          </label>
          <input
            type="url"
            placeholder={`Paste your ${deal.platformLabel} review link`}
            className={inputClass}
          />
        </div>
      )}

      <ScreenshotUpload
        label={isRating ? 'Rating Screenshot' : 'Review Screenshot'}
        hint={isRating
          ? 'Show the star rating you submitted on the product page.'
          : 'Ensure your username and review text are clearly visible.'}
      />

      <button className={submitBtnClass('bg-neon-purple hover:brightness-110')}>
        Submit {isRating ? 'Rating' : 'Review'}
      </button>
    </div>
  )
}

function ReturnStep() {
  return (
    <div className="space-y-5">
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        Upload a screenshot of return window completed page.
      </p>
      <ScreenshotUpload
        label="Return Window Completed Screenshot"
        hint="Ensure the order ID and product name are clearly visible."
      />
      <button className={submitBtnClass('bg-neon-cyan hover:brightness-110')}>
        Submit Screenshot
      </button>
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
}

export function ClaimStepForm({ deal, currentStep, onStepChange }: ClaimStepFormProps) {
  const step = CLAIM_STEPS[currentStep]

  function handleClaimSuccess(claim: ClaimResponseDto) {
    onStepChange(claim.currentStep ?? currentStep + 1)
  }

  return (
    <div className="space-y-5">
      <div>
        <h3 className="text-base font-bold text-ink-light-primary dark:text-ink-dark-primary">
          {step.label}
        </h3>
        <p className={['text-[10px] font-semibold uppercase tracking-wider mt-0.5', step.color].join(' ')}>
          Step {currentStep + 1} of {CLAIM_STEPS.length}
        </p>
      </div>

      {currentStep === 0 && <OrderStep deal={deal} onSuccess={handleClaimSuccess} />}
      {currentStep === 1 && <ReviewStep deal={deal} />}
      {currentStep === 2 && <ReturnStep />}
      {currentStep === 3 && <CashbackStep />}
    </div>
  )
}
