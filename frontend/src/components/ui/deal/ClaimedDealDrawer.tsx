import { useEffect, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { StepperStep } from '../Stepper'
import { fetchStepConfig } from '../../../api/campaignApi'
import { toStepperSteps } from '../../../constants/claimSteps'
import { StepperHeader } from '../StepperHeader'
import { DealSummaryRow } from './DealSummaryRow'
import { ClaimDeal } from './ClaimDeal'

interface ClaimedDealDrawerProps {
  deal: Deal
  onClose: () => void
}

export function ClaimedDealDrawer({ deal, onClose }: ClaimedDealDrawerProps) {
  const [steps, setSteps] = useState<StepperStep[]>([])

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose])

  useEffect(() => {
    fetchStepConfig().then(config => {
      setSteps(toStepperSteps(config[deal.dealType] ?? []))
    })
  }, [deal.dealType])

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Panel */}
      <div className="relative w-full sm:max-w-lg max-h-[90dvh] flex flex-col rounded-t-2xl sm:rounded-2xl bg-surface-light-base dark:bg-surface-dark-base border border-surface-light-border dark:border-surface-dark-border shadow-2xl overflow-hidden">

        <StepperHeader
          label="Claim Progress"
          steps={steps}
          currentStep={deal.currentStep ?? 0}
          onClose={onClose}
          className="px-5 pt-5 pb-4 border-b border-surface-light-border dark:border-surface-dark-border flex-shrink-0"
        />

        {/* Scrollable body */}
        <div className="overflow-y-auto flex-1">
          <DealSummaryRow deal={deal} className="px-5 py-4 border-b border-surface-light-border dark:border-surface-dark-border" />

          {/* Claim form */}
          <div className="p-5">
            <ClaimDeal deal={deal} />
          </div>
        </div>
      </div>
    </div>
  )
}
