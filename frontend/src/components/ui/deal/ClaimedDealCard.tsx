import type { Deal, ClaimStep } from '../../../types/DealTypes'
import type { Platform, CampaignType } from '../../../types/CampaignTypes'
import { CLAIM_STEPS } from '../../../constants/claim'
import { ClaimStatusBadge } from './ClaimStatusBadge'

const platformColors: Record<Platform, string> = {
  PLATFORM_AMAZON:   'text-neon-orange bg-neon-orange/10 border-neon-orange/25',
  PLATFORM_FLIPKART: 'text-neon-blue   bg-neon-blue/10   border-neon-blue/25',
  PLATFORM_NYKAA:    'text-neon-pink   bg-neon-pink/10   border-neon-pink/25',
  PLATFORM_MYNTRA:   'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

const dealTypeColors: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING:            'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/25',
  CAMPAIGN_TYPE_REVIEW:            'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/25',
  CAMPAIGN_TYPE_ORDER:             'text-neon-green  bg-neon-green/10  border-neon-green/25',
  CAMPAIGN_TYPE_DISCOUNT:          'text-neon-red    bg-neon-red/10    border-neon-red/25',
  CAMPAIGN_TYPE_AGENCY_DISCRETION: 'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

interface ClaimedDealCardProps {
  deal: Deal
  onClick: () => void
}

export function ClaimedDealCard({ deal, onClick }: ClaimedDealCardProps) {
  const claimStep = deal.claimStep ?? 1

  return (
    <div
      onClick={onClick}
      className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card hover:border-neon-blue/30 transition-colors cursor-pointer p-4"
    >
      <div className="flex items-center gap-4">
        {/* Thumbnail */}
        <img
          src={deal.productImageUrl}
          alt={deal.productName}
          className="w-14 h-14 rounded-xl object-cover flex-shrink-0 border border-surface-light-border dark:border-surface-dark-border"
        />

        {/* Main content */}
        <div className="flex-1 min-w-0 space-y-2">
          {/* Top row: name + status badge */}
          <div className="flex items-start justify-between gap-2">
            <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary line-clamp-2 leading-tight">
              {deal.productName}
            </p>
            {deal.claimStatus && (
              <div className="flex-shrink-0">
                <ClaimStatusBadge status={deal.claimStatus} />
              </div>
            )}
          </div>

          {/* Badges */}
          <div className="flex items-center gap-1.5 flex-wrap">
            <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', platformColors[deal.platform]].join(' ')}>
              {deal.platformLabel}
            </span>
            <span className={['text-[10px] font-semibold px-2 py-0.5 rounded-full border', dealTypeColors[deal.dealType]].join(' ')}>
              {deal.dealTypeLabel}
            </span>
          </div>

          {/* Mini stepper */}
          <MiniStepper claimStep={claimStep} />
        </div>
      </div>
    </div>
  )
}

function MiniStepper({ claimStep }: { claimStep: ClaimStep }) {
  return (
    <div className="flex items-center gap-0">
      {CLAIM_STEPS.map((step, i) => {
        const done   = step.number < claimStep
        const active = step.number === claimStep
        const locked = step.number > claimStep

        return (
          <div key={step.number} className="flex items-center">
            <div className={[
              'w-5 h-5 rounded-full flex items-center justify-center text-[9px] font-bold flex-shrink-0',
              done   ? 'bg-neon-blue text-white'                                              : '',
              active ? 'bg-neon-blue/20 border-2 border-neon-blue text-neon-blue'            : '',
              locked ? 'bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border text-ink-light-muted dark:text-ink-dark-muted' : '',
            ].join(' ')}>
              {done ? '✓' : step.number}
            </div>
            {i < CLAIM_STEPS.length - 1 && (
              <div className={[
                'w-5 h-px',
                step.number < claimStep ? 'bg-neon-blue' : 'bg-surface-light-border dark:bg-surface-dark-border',
              ].join(' ')} />
            )}
          </div>
        )
      })}
    </div>
  )
}
