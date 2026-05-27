import { useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import { ClaimStepForm } from './ClaimStepForm'

interface ClaimDealProps {
  deal: Deal
  initialStep?: number
}

export function ClaimDeal({ deal, initialStep = 0 }: ClaimDealProps) {
  const [currentStep, setCurrentStep] = useState(initialStep)

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 overflow-y-auto">
      <ClaimStepForm
        deal={deal}
        currentStep={currentStep}
        onStepChange={setCurrentStep}
      />
    </div>
  )
}
