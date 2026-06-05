import { useEffect, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import type { StepperStep } from '../Stepper'
import { fetchStepConfig } from '../../../api/campaignApi'
import { toStepperSteps } from '../../../constants/claimSteps'
import { IconChevronRight } from '../icons'
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
  const [activeStep, setActiveStep] = useState(claimResponse?.currentStep ?? deal.currentStep ?? 0)
  const [viewedStep, setViewedStep] = useState(activeStep)
  const [steps, setSteps] = useState<StepperStep[]>([])

  useEffect(() => {
    fetchStepConfig().then(config => {
      setSteps(toStepperSteps(config[deal.dealType] ?? []))
    })
  }, [deal.dealType])

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted">
        <button onClick={onBack} className="hover:text-neon-blue transition-colors">
          Deals
        </button>
        <IconChevronRight size={12} />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium truncate">
          {deal.productName}
        </span>
      </div>

      <StepperHeader
        label="Claim Progress"
        steps={steps}
        currentStep={activeStep}
        onStepClick={setViewedStep}
        className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card px-5 py-4"
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-14rem)]">
        <DealInfo deal={deal} />
        <ClaimDeal
          key={viewedStep}
          deal={deal}
          initialStep={viewedStep}
          readOnly={viewedStep < activeStep}
          claimResponse={claimResponse}
          onStepChange={step => { setActiveStep(step); setViewedStep(step) }}
        />
      </div>
    </div>
  )
}
