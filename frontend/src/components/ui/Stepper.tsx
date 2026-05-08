export interface StepperStep {
  label: string
  color: string
  dotColor: string
  lineColor: string
}

interface StepperProps {
  steps: StepperStep[]
  currentStep: number
  onStepClick?: (index: number) => void
}

export function Stepper({ steps, currentStep, onStepClick }: StepperProps) {
  return (
    <div className="flex items-center">
      {steps.map((step, i) => {
        const done    = i < currentStep
        const active  = i === currentStep
        const clickable = !!onStepClick

        return (
          <div
            key={step.label}
            className="flex items-center"
            style={{ flex: i < steps.length - 1 ? '1' : undefined }}
          >
            <div
              className={['flex flex-col items-center gap-1 shrink-0', clickable ? 'cursor-pointer group' : ''].join(' ')}
              onClick={() => onStepClick?.(i)}
            >
              <div className={[
                'w-3 h-3 rounded-full border-2 transition-colors',
                done   ? `${step.dotColor} border-transparent` : '',
                active ? `border-current ${step.color} bg-transparent ring-2 ring-current ring-offset-1 ring-offset-surface-light-card dark:ring-offset-surface-dark-card` : '',
                !done && !active ? 'bg-surface-light-hover dark:bg-surface-dark-hover border-surface-light-border dark:border-surface-dark-border' : '',
                clickable && !active ? 'group-hover:scale-125' : '',
              ].join(' ')} />
              <span className={[
                'text-[9px] font-medium whitespace-nowrap transition-colors',
                done || active ? step.color : 'text-ink-light-muted dark:text-ink-dark-muted',
                clickable && !active ? `group-hover:${step.color}` : '',
              ].join(' ')}>
                {step.label}
              </span>
            </div>

            {i < steps.length - 1 && (
              <div className={[
                'h-0.5 flex-1 mx-1 rounded-full mb-4',
                done ? step.lineColor : 'bg-surface-light-hover dark:bg-surface-dark-hover',
              ].join(' ')} />
            )}
          </div>
        )
      })}
    </div>
  )
}
