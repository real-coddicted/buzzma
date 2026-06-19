import { useEffect, useState } from 'react'
import type { Deal } from '../../../types/DealTypes'
import type { StepperStep } from '../Stepper'
import { fetchStepConfig } from '../../../api/campaignApi'
import { toStepperSteps, getStepVerificationStatuses } from '../../../constants/claimSteps'
import { PLATFORM_COLORS, DEAL_TYPE_COLORS } from '../../../constants/deal'
import { REVIEW_STATUS_CONFIG } from '../claim-review/claimReviewConstants'
import { ProductThumbnail } from './ProductThumbnail'
import { Stepper } from '../Stepper'
import { paiseToRupees, formatRupees } from '../../../utils/currency'

interface ClaimedDealListItemProps {
  deal: Deal
  currentStep?: number
  onClick?: () => void
}

export function ClaimedDealListItem({ deal, currentStep = 0, onClick }: ClaimedDealListItemProps) {
  const [steps, setSteps] = useState<StepperStep[]>([])
  const [rawStepTypes, setRawStepTypes] = useState<string[]>([])

  useEffect(() => {
    fetchStepConfig().then(config => {
      const cfg = config[deal.dealType] ?? []
      setSteps(toStepperSteps(cfg))
      setRawStepTypes(cfg.map((s: { type: string }) => s.type))
    })
  }, [deal.dealType])

  const stepStatuses = getStepVerificationStatuses(rawStepTypes, deal.screenshots ?? [])

  return (
    <div
      onClick={onClick}
      className={[
        'flex flex-col sm:flex-row gap-4 p-4 rounded-2xl border',
        'border-surface-light-border dark:border-surface-dark-border',
        'bg-surface-light-card dark:bg-surface-dark-card',
        onClick ? 'cursor-pointer hover:border-neon-blue/30 transition-colors' : '',
      ].join(' ')}
    >
      <ProductThumbnail
        src={deal.productImageUrl}
        alt={deal.productName}
        className="w-full sm:w-20 h-20 shrink-0 rounded-xl"
      />

      <div className="flex-1 min-w-0 space-y-3">
        <div className="flex items-start gap-2 justify-between">
          <div className="min-w-0">
            <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary leading-snug truncate">
              {deal.productName}
            </p>
            <div className="flex items-center gap-1.5 mt-1 flex-wrap">
              <span className={[
                'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
                PLATFORM_COLORS[deal.platform],
              ].join(' ')}>
                {deal.platformLabel}
              </span>
              <span className={[
                'text-[10px] font-semibold px-2 py-0.5 rounded-full border',
                DEAL_TYPE_COLORS[deal.dealType],
              ].join(' ')}>
                {deal.dealTypeLabel}
              </span>
              {deal.reviewStatus && deal.reviewStatus !== 'pending' && (
                <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full border ${REVIEW_STATUS_CONFIG[deal.reviewStatus].classes}`}>
                  {REVIEW_STATUS_CONFIG[deal.reviewStatus].label}
                </span>
              )}
            </div>
          </div>
          <span className="text-sm font-bold text-neon-green shrink-0">
            ₹{formatRupees(paiseToRupees(deal.offeredPricePaise))}
          </span>
        </div>

        {steps.length > 0 && <Stepper steps={steps} currentStep={currentStep} stepStatuses={stepStatuses} />}
      </div>
    </div>
  )
}
