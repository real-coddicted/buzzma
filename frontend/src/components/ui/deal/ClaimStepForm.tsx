import { useEffect, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import type { CampaignStepDto } from '../../../api/campaignApi'
import { fetchCampaignStepConfig } from '../../../api/campaignApi'
import { STEP_TYPE_COLORS, STEP_TYPE_TO_SCREENSHOT_TYPE, getStepVerificationStatuses } from '../../../constants/claimSteps'
import { ScreenshotRejectionBanner } from '../ScreenshotRejectionBanner'
import { OrderStep } from './OrderStep'
import { DeliveryStep } from './DeliveryStep'
import { RatingStep } from './RatingStep'
import { ReviewStep } from './ReviewStep'
import { SellerFeedbackStep } from './SellerFeedbackStep'
import { ReturnStep } from './ReturnStep'
import { CashbackStep } from './CashbackStep'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']

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
    fetchCampaignStepConfig(deal.campaignId).then(setSteps)
  }, [deal.campaignId])

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
    const nextRejected = getStepVerificationStatuses(steps.map(s => s.type), claim.screenshots ?? [])
      .findIndex(s => s === 'rejected')
    onStepChange(nextRejected !== -1 ? nextRejected : (claim.currentStep ?? currentStep + 1))
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

      {rejectedScreenshot?.reviewerComments && (
        <ScreenshotRejectionBanner comment={rejectedScreenshot.reviewerComments} label={step?.label} />
      )}

      {stepType === 'ORDER'         && <OrderStep  deal={deal} claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'DELIVERY'      && <DeliveryStep claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'RATING'        && <RatingStep deal={deal} claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'REVIEW'        && <ReviewStep deal={deal} claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'SELLER_FEEDBACK' && <SellerFeedbackStep claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'RETURN_WINDOW' && <ReturnStep claimId={effectiveClaim?.id} onSuccess={handleClaimSuccess} readOnly={readOnly} claimResponse={effectiveClaim} rejectedScreenshot={rejectedScreenshot} />}
      {stepType === 'CASHBACK'      && <CashbackStep />}
    </div>
  )
}
