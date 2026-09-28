import { useEffect, useState } from 'react'
import { fetchStepConfig, type CampaignStepDto } from '../../../api/campaignApi'
import type { PromotionCategory } from '../../../types'
import { PROMOTION_CATEGORY_STEPS, PROMOTION_CATEGORY_FORCED_STEP } from '../../../constants/campaigns'

interface FormSlice {
  category: PromotionCategory
  requiredSteps: string[]
}

interface Props {
  form: FormSlice
  set: (field: keyof FormSlice, value: unknown) => void
  readOnly?: boolean
}

export function CampaignRequiredScreenshotsFields({ form, set, readOnly }: Props) {
  const [allSteps, setAllSteps] = useState<CampaignStepDto[]>([])

  useEffect(() => {
    fetchStepConfig().then(setAllSteps)
  }, [])

  const allowedSteps = PROMOTION_CATEGORY_STEPS[form.category]
  const forcedStep = PROMOTION_CATEGORY_FORCED_STEP[form.category]
  const selectableSteps = allSteps.filter(step => allowedSteps.includes(step.type))

  function handleStepToggle(type: string, checked: boolean) {
    set('requiredSteps', checked
      ? [...form.requiredSteps, type]
      : form.requiredSteps.filter(t => t !== type))
  }

  return (
    <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-orange">Required Screenshots</h3>
      <div className="space-y-2">
        {selectableSteps.map(step => (
          <label
            key={step.type}
            className="flex items-center gap-2 text-xs text-ink-light-primary dark:text-ink-dark-primary"
          >
            <input
              type="checkbox"
              checked={form.requiredSteps.includes(step.type)}
              disabled={readOnly || step.type === forcedStep}
              onChange={e => handleStepToggle(step.type, e.target.checked)}
            />
            {step.label}
          </label>
        ))}
      </div>
    </section>
  )
}
