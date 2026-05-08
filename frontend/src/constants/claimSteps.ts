import type { StepperStep } from '../components/ui/Stepper'

export const CLAIM_STEPS: StepperStep[] = [
  { label: 'Order & Upload',    color: 'text-neon-blue',   dotColor: 'bg-neon-blue',   lineColor: 'bg-neon-blue/40'   },
  { label: 'Rating/Review',   color: 'text-neon-purple', dotColor: 'bg-neon-purple', lineColor: 'bg-neon-purple/40' },
  { label: 'Return',   color: 'text-neon-orange',   dotColor: 'bg-neon-orange',   lineColor: 'bg-neon-orange/40'   },
  { label: 'Cash', color: 'text-neon-green',  dotColor: 'bg-neon-green',  lineColor: 'bg-neon-green/40'  },
]
