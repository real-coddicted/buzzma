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
  onStepChange?: (step: number) => void
  onClaimUpdate?: (claim: ClaimResponseDto) => void
}

export function ClaimDeal({ deal, initialStep = 0, readOnly = false, claimResponse, onStepChange, onClaimUpdate }: ClaimDealProps) {
  const [currentStep, setCurrentStep] = useState(initialStep)

  function handleStepChange(step: number) {
    setCurrentStep(step)
    onStepChange?.(step)
  }

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 overflow-y-auto">
      <ClaimStepForm
        deal={deal}
        currentStep={currentStep}
        onStepChange={handleStepChange}
        onClaimUpdate={onClaimUpdate}
        readOnly={readOnly}
        claimResponse={claimResponse}
      />
    </div>
  )
}
