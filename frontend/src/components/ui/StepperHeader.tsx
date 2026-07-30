import type { StepperStep, StepVerificationStatus } from './Stepper'
import { Stepper } from './Stepper'
import { CopyableCode } from './CopyableCode'

interface StepperHeaderProps {
  label: string
  code?: string
  steps: StepperStep[]
  currentStep: number
  onClose?: () => void
  onStepClick?: (index: number) => void
  stepStatuses?: Array<StepVerificationStatus | undefined>
  className?: string
}

export function StepperHeader({ label, code, steps, currentStep, onClose, onStepClick, stepStatuses, className = '' }: StepperHeaderProps) {
  return (
    <div className={className}>
      <div className="flex items-center justify-between mb-1">
        <div className="flex items-center gap-2">
          <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">
            {label}
          </p>
          {code && <CopyableCode code={code} />}
        </div>
        {onClose && (
          <button
            onClick={onClose}
            className="text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors text-lg leading-none"
          >
            ✕
          </button>
        )}
      </div>
      <Stepper steps={steps} currentStep={currentStep} onStepClick={onStepClick} stepStatuses={stepStatuses} />
    </div>
  )
}
