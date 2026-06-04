import type { StepperStep } from '../components/ui/Stepper'
import type { CampaignStepDto } from '../api/campaignApi'

const FALLBACK_COLORS: Omit<StepperStep, 'label'> = {
  color: 'text-neon-blue', dotColor: 'bg-neon-blue', lineColor: 'bg-neon-blue/40',
}

export const STEP_TYPE_COLORS: Record<string, Omit<StepperStep, 'label'>> = {
  ORDER:         { color: 'text-neon-blue',   dotColor: 'bg-neon-blue',   lineColor: 'bg-neon-blue/40'   },
  RATING:        { color: 'text-neon-purple', dotColor: 'bg-neon-purple', lineColor: 'bg-neon-purple/40' },
  REVIEW:        { color: 'text-neon-cyan',   dotColor: 'bg-neon-cyan',   lineColor: 'bg-neon-cyan/40'   },
  RETURN_WINDOW: { color: 'text-neon-orange', dotColor: 'bg-neon-orange', lineColor: 'bg-neon-orange/40' },
  CASHBACK:      { color: 'text-neon-green',  dotColor: 'bg-neon-green',  lineColor: 'bg-neon-green/40'  },
}

export function toStepperSteps(steps: CampaignStepDto[]): StepperStep[] {
  return steps.map(s => ({ label: s.label, ...(STEP_TYPE_COLORS[s.type] ?? FALLBACK_COLORS) }))
}
