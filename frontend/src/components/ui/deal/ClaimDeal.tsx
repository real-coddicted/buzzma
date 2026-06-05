import { useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { ClaimStepForm } from './ClaimStepForm'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']

interface ClaimDealProps {
  deal: Deal
  initialStep?: number
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
}

export function ClaimDeal({ deal, initialStep = 0, readOnly = false, claimResponse }: ClaimDealProps) {
  const [currentStep, setCurrentStep] = useState(initialStep)

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 overflow-y-auto">
      <ClaimStepForm
        deal={deal}
        currentStep={currentStep}
        onStepChange={setCurrentStep}
        readOnly={readOnly}
        claimResponse={claimResponse}
      />
    </div>
  )
}
