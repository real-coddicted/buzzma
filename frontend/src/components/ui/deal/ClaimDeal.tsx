import type { Deal } from '../../../types/DealTypes'
import { ClaimStepForm } from './ClaimStepForm'

interface ClaimDealProps {
  deal: Deal
  currentStep?: number
}

export function ClaimDeal({ deal, currentStep = 0 }: ClaimDealProps) {
  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 overflow-y-auto">
      <ClaimStepForm deal={deal} currentStep={currentStep} />
    </div>
  )
}
