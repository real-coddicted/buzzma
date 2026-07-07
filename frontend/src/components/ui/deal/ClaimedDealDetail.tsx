import { useEffect, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import type { StepperStep } from '../Stepper'
import { fetchStepConfig } from '../../../api/campaignApi'
import { toStepperSteps, getStepVerificationStatuses } from '../../../constants/claimSteps'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import { StepperHeader } from '../StepperHeader'
import { DealInfo } from './DealInfo'
import { ClaimDeal } from './ClaimDeal'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']

interface ClaimedDealDetailProps {
  deal: Deal
  onBack: () => void
  claimResponse?: ClaimResponseDto
}

export function ClaimedDealDetail({ deal, onBack, claimResponse }: ClaimedDealDetailProps) {
  const [effectiveClaim, setEffectiveClaim] = useState(claimResponse)
  const [activeStep, setActiveStep] = useState(claimResponse?.currentStep ?? deal.currentStep ?? 0)
  const [viewedStep, setViewedStep] = useState(activeStep)
  const [steps, setSteps] = useState<StepperStep[]>([])
  const [rawStepTypes, setRawStepTypes] = useState<string[]>([])

  useEffect(() => {
    fetchStepConfig().then(config => {
      const cfg = config[deal.dealType] ?? []
      setSteps(toStepperSteps(cfg))
      setRawStepTypes(cfg.map(s => s.type))
    })
  }, [deal.dealType])

  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(deal.productName, onBack)
    return clearDetail
  }, [deal.productName, onBack, setDetail, clearDetail])

  const stepStatuses = getStepVerificationStatuses(rawStepTypes, effectiveClaim?.screenshots ?? [])
  const viewedStepRejected = stepStatuses[viewedStep] === 'rejected'
  const claimRejected = effectiveClaim?.status === 'REJECTED'
  const claimApproved = effectiveClaim?.status === 'APPROVED'
    || effectiveClaim?.status === 'REWARD_PENDING'
    || effectiveClaim?.status === 'COMPLETED'

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <StepperHeader
        label="Claim Progress"
        steps={steps}
        currentStep={activeStep}
        onStepClick={setViewedStep}
        stepStatuses={stepStatuses}
        className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card px-5 py-4"
      />

      {claimRejected && (
        <div className="rounded-xl border border-neon-red/30 bg-neon-red/10 px-4 py-3 text-sm text-neon-red space-y-0.5">
          <p className="font-semibold">Your claim has been rejected</p>
          {effectiveClaim?.reviewerComments && (
            <p className="text-neon-red/80">{effectiveClaim.reviewerComments}</p>
          )}
        </div>
      )}

      {claimApproved && (
        <div className="rounded-xl border border-neon-green/30 bg-neon-green/10 px-4 py-3 text-sm text-neon-green">
          <p className="font-semibold">Your claim has been approved</p>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-14rem)]">
        <DealInfo deal={deal} />
        <ClaimDeal
          key={viewedStep}
          deal={deal}
          initialStep={viewedStep}
          readOnly={claimRejected || (viewedStep < activeStep && !viewedStepRejected)}
          claimResponse={effectiveClaim}
          onStepChange={step => { setActiveStep(step); setViewedStep(step) }}
          onClaimUpdate={setEffectiveClaim}
        />
      </div>
    </div>
  )
}
