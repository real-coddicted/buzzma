import { labelClass, inputClass, errorClass } from './campaignFormConstants'

interface FormSlice {
  originalPriceRupees: string
  campaignPriceRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: string) => void
}

export function CampaignPricingFields({ form, errors, set }: Props) {
  return (
    <div className="space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-green">Pricing (₹)</h3>
      <div className="space-y-4">
        <div>
          <label className={labelClass}>Original Price *</label>
          <div className="relative">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs text-ink-light-muted dark:text-ink-dark-muted">₹</span>
            <input className={[inputClass, 'pl-6'].join(' ')} type="number" min="0" step="0.01" placeholder="0.00" value={form.originalPriceRupees} onChange={e => set('originalPriceRupees', e.target.value)} />
          </div>
          {errors.originalPriceRupees && <p className={errorClass}>{errors.originalPriceRupees}</p>}
        </div>
        <div>
          <label className={labelClass}>Campaign Price *</label>
          <div className="relative">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs text-ink-light-muted dark:text-ink-dark-muted">₹</span>
            <input className={[inputClass, 'pl-6'].join(' ')} type="number" min="0" step="0.01" placeholder="0.00" value={form.campaignPriceRupees} onChange={e => set('campaignPriceRupees', e.target.value)} />
          </div>
          {errors.campaignPriceRupees && <p className={errorClass}>{errors.campaignPriceRupees}</p>}
        </div>
      </div>
    </div>
  )
}
