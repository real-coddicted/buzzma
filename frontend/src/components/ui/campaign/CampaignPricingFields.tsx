import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { RupeeInput } from '../RupeeInput'

interface FormSlice {
  originalPriceRupees: string
  campaignPriceRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: string) => void
  readOnly?: boolean
}

export function CampaignPricingFields({ form, errors, set, readOnly }: Props) {
  return (
    <div className="space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-green">Pricing (₹)</h3>
      <div className="space-y-4">
        <div>
          <label className={labelClass}>Original Price *</label>
          <RupeeInput value={form.originalPriceRupees} onChange={v => set('originalPriceRupees', v)} className={inputClass} disabled={readOnly} />
          {errors.originalPriceRupees && <p className={errorClass}>{errors.originalPriceRupees}</p>}
        </div>
        <div>
          <label className={labelClass}>Campaign Price *</label>
          <RupeeInput value={form.campaignPriceRupees} onChange={v => set('campaignPriceRupees', v)} className={inputClass} disabled={readOnly} />
          {errors.campaignPriceRupees && <p className={errorClass}>{errors.campaignPriceRupees}</p>}
        </div>
      </div>
    </div>
  )
}
