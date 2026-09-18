import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { RupeeInput } from '../RupeeInput'

interface FormSlice {
  campaignPriceRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: string) => void
  readOnly?: boolean
}

export function CampaignIncentiveFields({ form, errors, set, readOnly }: Props) {
  return (
    <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-green">Incentives</h3>
      <div>
        <label className={labelClass}>Campaign Price *</label>
        <RupeeInput value={form.campaignPriceRupees} onChange={v => set('campaignPriceRupees', v)} className={inputClass} disabled={readOnly} />
        {errors.campaignPriceRupees && <p className={errorClass}>{errors.campaignPriceRupees}</p>}
      </div>
    </section>
  )
}
