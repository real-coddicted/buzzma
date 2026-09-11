import type { Platform, CampaignType, PromotionCategory } from '../../../types'
import {
  CAMPAIGN_TYPE_LABELS,
  PROMOTION_CATEGORY_PLATFORMS,
  PROMOTION_CATEGORY_CAMPAIGN_TYPES,
  PROMOTION_CATEGORY_STEPS,
} from '../../../constants/campaigns'
import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { PromotionCategorySelector } from './PromotionCategorySelector'

interface FormSlice {
  title: string
  category: PromotionCategory
  platform: Platform | ''
  campaignType: CampaignType | ''
  startDate: string
  endDate: string
  commissionToAllRupees: string
  requiredSteps: string[]
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: unknown) => void
  readOnly?: boolean
}

export function CampaignInfoFields({ form, errors, set, readOnly }: Props) {
  const typeOptions = PROMOTION_CATEGORY_CAMPAIGN_TYPES[form.category]

  function onCategoryChange(category: PromotionCategory) {
    set('category', category)
    if (!PROMOTION_CATEGORY_PLATFORMS[category].includes(form.platform as Platform)) {
      set('platform', '')
    }
    if (!PROMOTION_CATEGORY_CAMPAIGN_TYPES[category].includes(form.campaignType as CampaignType)) {
      set('campaignType', '')
    }
    const allowedSteps = PROMOTION_CATEGORY_STEPS[category]
    set('requiredSteps', form.requiredSteps.filter(s => allowedSteps.includes(s)))
  }

  return (
    <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-blue">Basic Info</h3>
      <div>
        <label className={labelClass}>Promotion Category *</label>
        <PromotionCategorySelector value={form.category} onChange={onCategoryChange} disabled={readOnly} />
      </div>
      <div>
        <label className={labelClass}>Title *</label>
        <input className={inputClass} type="text" placeholder="e.g. Summer Sale 2025" value={form.title} onChange={e => set('title', e.target.value)} disabled={readOnly} />
        {errors.title && <p className={errorClass}>{errors.title}</p>}
      </div>
      <div>
        <label className={labelClass}>Campaign Type</label>
        <select className={inputClass} value={form.campaignType} onChange={e => set('campaignType', e.target.value as CampaignType | '')} disabled={readOnly}>
          <option value="">— None —</option>
          {typeOptions.map(k => (
            <option key={k} value={k}>{CAMPAIGN_TYPE_LABELS[k]}</option>
          ))}
        </select>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelClass}>Start Date *</label>
          <input className={inputClass} type="date" value={form.startDate} onChange={e => set('startDate', e.target.value)} disabled={readOnly} />
          {errors.startDate && <p className={errorClass}>{errors.startDate}</p>}
        </div>
        <div>
          <label className={labelClass}>End Date *</label>
          <input className={inputClass} type="date" value={form.endDate} onChange={e => set('endDate', e.target.value)} disabled={readOnly} />
          {errors.endDate && <p className={errorClass}>{errors.endDate}</p>}
        </div>
      </div>
    </section>
  )
}
