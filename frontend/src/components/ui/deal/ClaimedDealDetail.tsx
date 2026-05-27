import { useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import { CLAIM_STEPS } from '../../../constants/claimSteps'
import { IconChevronRight } from '../icons'
import { StepperHeader } from '../StepperHeader'
import { DealInfo } from './DealInfo'
import { ClaimDeal } from './ClaimDeal'

interface ClaimedDealDetailProps {
  deal: Deal
  onBack: () => void
}

export function ClaimedDealDetail({ deal, onBack }: ClaimedDealDetailProps) {
  const [currentStep, setCurrentStep] = useState(deal.currentStep ?? 0)

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
        steps={CLAIM_STEPS}
        currentStep={currentStep}
        onStepClick={setCurrentStep}
        className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card px-5 py-4"
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-14rem)]">
        <DealInfo deal={deal} />
        <ClaimDeal deal={deal} initialStep={currentStep} />
      </div>
    </div>
  )
}
