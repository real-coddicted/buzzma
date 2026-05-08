import type { StepperStep } from './Stepper'
import { Stepper } from './Stepper'

interface StepperHeaderProps {
  label: string
  steps: StepperStep[]
  currentStep: number
  onClose?: () => void
  onStepClick?: (index: number) => void
  className?: string
}

export function StepperHeader({ label, steps, currentStep, onClose, onStepClick, className = '' }: StepperHeaderProps) {
  return (
    <div className={className}>
      <div className="flex items-center justify-between mb-1">
        <p className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">
          {label}
        </p>
        {onClose && (
          <button
            onClick={onClose}
            className="text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary transition-colors text-lg leading-none"
          >
            ✕
          </button>
        )}
      </div>
      <Stepper steps={steps} currentStep={currentStep} onStepClick={onStepClick} />
    </div>
  )
}
