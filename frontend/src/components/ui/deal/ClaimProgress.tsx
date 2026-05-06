import { useState } from 'react'
import type { Deal, ClaimStep } from '../../../types/DealTypes'
import { CLAIM_STEPS } from '../../../constants/claim'
import { ClaimStepOrderPlaced }   from './ClaimStepOrderPlaced'
import { ClaimStepProofUpload }   from './ClaimStepProofUpload'
import { ClaimStepUnderReview }   from './ClaimStepUnderReview'
import { ClaimStepCashDisbursed } from './ClaimStepCashDisbursed'

interface ClaimProgressProps {
  deal: Deal
}

export function ClaimProgress({ deal }: ClaimProgressProps) {
  const claimStep = deal.claimStep ?? 1
  const [viewStep, setViewStep] = useState<ClaimStep>(claimStep)

  function handleStepClick(n: ClaimStep) {
    if (n <= claimStep) setViewStep(n)
  }

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card overflow-hidden flex flex-col">
      {/* Horizontal stepper strip */}
      <div className="px-5 pt-5 pb-4 flex-shrink-0 border-b border-surface-light-border dark:border-surface-dark-border">
        <div className="flex items-start">
          {CLAIM_STEPS.map((step, i) => {
            const done   = step.number < claimStep
            const active = step.number === claimStep
            const locked = step.number > claimStep
            const viewing = step.number === viewStep

            return (
              <div key={step.number} className="flex items-start flex-1">
                <div className="flex flex-col items-center gap-1.5 flex-shrink-0">
                  <button
                    onClick={() => handleStepClick(step.number)}
                    disabled={locked}
                    className={[
                      'w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all',
                      done   ? 'bg-neon-blue text-white'                                                         : '',
                      active && viewing  ? 'bg-neon-blue/20 border-2 border-neon-blue text-neon-blue ring-2 ring-neon-blue/20' : '',
                      active && !viewing ? 'bg-neon-blue/20 border-2 border-neon-blue text-neon-blue'           : '',
                      locked ? 'bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border text-ink-light-muted dark:text-ink-dark-muted cursor-not-allowed' : '',
                      done && viewing ? 'ring-2 ring-neon-blue/20' : '',
                      !locked ? 'cursor-pointer hover:brightness-110' : '',
                    ].join(' ')}
                  >
                    {done ? '✓' : step.number}
                  </button>
                  <span className={[
                    'text-[9px] font-medium text-center leading-tight w-14 hidden sm:block',
                    locked ? 'text-ink-light-muted dark:text-ink-dark-muted' : 'text-ink-light-secondary dark:text-ink-dark-secondary',
                  ].join(' ')}>
                    {step.label}
                  </span>
                </div>

                {/* Connector line */}
                {i < CLAIM_STEPS.length - 1 && (
                  <div className={[
                    'flex-1 h-0.5 mt-4 mx-1',
                    step.number < claimStep ? 'bg-neon-blue' : 'bg-surface-light-border dark:bg-surface-dark-border',
                  ].join(' ')} />
                )}
              </div>
            )
          })}
        </div>
      </div>

      {/* Step content */}
      <div className="p-5 overflow-y-auto flex-1">
        {viewStep === 1 && <ClaimStepOrderPlaced   deal={deal} />}
        {viewStep === 2 && <ClaimStepProofUpload   deal={deal} />}
        {viewStep === 3 && <ClaimStepUnderReview   deal={deal} />}
        {viewStep === 4 && <ClaimStepCashDisbursed deal={deal} />}
      </div>
    </div>
  )
}
