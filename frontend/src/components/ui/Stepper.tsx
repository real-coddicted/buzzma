import { IconCheck, IconX } from './icons'

export type StepVerificationStatus = 'verified' | 'rejected'

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
  stepStatuses?: Array<StepVerificationStatus | undefined>
}

export function Stepper({ steps, currentStep, onStepClick, stepStatuses }: StepperProps) {
  const firstRejectedIndex = stepStatuses?.findIndex(s => s === 'rejected') ?? -1

  return (
    <div className="flex items-center">
      {steps.map((step, i) => {
        const done        = i < currentStep
        const active      = i === currentStep
        const status      = stepStatuses?.[i]
        const blocked     = firstRejectedIndex !== -1 && i > firstRejectedIndex
        const lineBlocked = firstRejectedIndex !== -1 && i >= firstRejectedIndex
        const clickable   = !!onStepClick && i <= currentStep && !blocked

        return (
          <div
            key={step.label}
            className="flex items-center"
            style={{ flex: i < steps.length - 1 ? '1' : undefined }}
          >
            <div
              className={['flex flex-col items-center gap-1 shrink-0', clickable ? 'cursor-pointer group' : ''].join(' ')}
              onClick={clickable ? () => onStepClick!(i) : undefined}
            >
              {status === 'verified' ? (
                <div className="rounded-full bg-neon-green p-0.5 flex items-center justify-center" title="Screenshot verified">
                  <IconCheck size={10} className="text-white" />
                </div>
              ) : status === 'rejected' ? (
                <div className="relative w-3.5 h-3.5" title="Screenshot rejected">
                  <span className="absolute inset-0 rounded-full bg-neon-red/50 animate-ping" />
                  <div className="relative w-3.5 h-3.5 rounded-full bg-neon-red p-0.5 flex items-center justify-center ring-2 ring-neon-red/40 ring-offset-1 ring-offset-surface-light-card dark:ring-offset-surface-dark-card">
                    <IconX size={10} className="text-white" />
                  </div>
                </div>
              ) : (
                <div className={[
                  'w-3 h-3 rounded-full border-2 transition-colors',
                  done   ? `${step.dotColor} border-transparent` : '',
                  active ? `border-current ${step.color} bg-transparent ring-2 ring-current ring-offset-1 ring-offset-surface-light-card dark:ring-offset-surface-dark-card` : '',
                  !done && !active ? (blocked
                    ? 'bg-neon-red/10 border-neon-red/30'
                    : 'bg-surface-light-hover dark:bg-surface-dark-hover border-surface-light-border dark:border-surface-dark-border') : '',
                  clickable && !active ? 'group-hover:scale-125' : '',
                ].join(' ')} />
              )}
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
                lineBlocked ? 'bg-neon-red/50' : status === 'verified' ? 'bg-neon-green/50' : done ? step.lineColor : 'bg-surface-light-hover dark:bg-surface-dark-hover',
              ].join(' ')} />
            )}
          </div>
        )
      })}
    </div>
  )
}
