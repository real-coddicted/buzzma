import type { Platform, CampaignType } from '../../../types'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../../../constants/campaigns'
import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { CampaignPricingFields } from './CampaignPricingFields'

interface FormSlice {
  title: string
  platform: Platform | ''
  campaignType: CampaignType | ''
  startDate: string
  endDate: string
  productBrandName: string
  productName: string
  productUrl: string
  productImageUrl: string
  sellerName: string
  originalPriceRupees: string
  campaignPriceRupees: string
  commissionToAllRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: string) => void
}

export function CampaignInfoFields({ form, errors, set }: Props) {
  return (
    <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-blue">Basic Info</h3>
      <div>
        <label className={labelClass}>Title *</label>
        <input className={inputClass} type="text" placeholder="e.g. Summer Sale 2025" value={form.title} onChange={e => set('title', e.target.value)} />
        {errors.title && <p className={errorClass}>{errors.title}</p>}
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelClass}>Platform *</label>
          <select className={inputClass} value={form.platform} onChange={e => set('platform', e.target.value as Platform | '')}>
            <option value="">— Select —</option>
            {(Object.keys(PLATFORM_LABELS) as Platform[]).map(k => (
              <option key={k} value={k}>{PLATFORM_LABELS[k]}</option>
            ))}
          </select>
          {errors.platform && <p className={errorClass}>{errors.platform}</p>}
        </div>
        <div>
          <label className={labelClass}>Campaign Type</label>
          <select className={inputClass} value={form.campaignType} onChange={e => set('campaignType', e.target.value as CampaignType | '')}>
            <option value="">— None —</option>
            {(Object.keys(CAMPAIGN_TYPE_LABELS) as CampaignType[]).map(k => (
              <option key={k} value={k}>{CAMPAIGN_TYPE_LABELS[k]}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelClass}>Start Date *</label>
          <input className={inputClass} type="date" value={form.startDate} onChange={e => set('startDate', e.target.value)} />
          {errors.startDate && <p className={errorClass}>{errors.startDate}</p>}
        </div>
        <div>
          <label className={labelClass}>End Date *</label>
          <input className={inputClass} type="date" value={form.endDate} onChange={e => set('endDate', e.target.value)} />
          {errors.endDate && <p className={errorClass}>{errors.endDate}</p>}
        </div>
      </div>

      <div>
        <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-cyan mb-4">Product</h3>
        <div className="space-y-4">
          <div>
            <label className={labelClass}>Brand Name *</label>
            <input className={inputClass} type="text" placeholder="e.g. Acme Co." value={form.productBrandName} onChange={e => set('productBrandName', e.target.value)} />
            {errors.productBrandName && <p className={errorClass}>{errors.productBrandName}</p>}
          </div>
          <div>
            <label className={labelClass}>Seller Name</label>
            <input className={inputClass} type="text" placeholder="e.g. Acme Store" value={form.sellerName} onChange={e => set('sellerName', e.target.value)} />
          </div>
          <div>
            <label className={labelClass}>Product Name *</label>
            <input className={inputClass} type="text" placeholder="e.g. Acme Coffee Cup" value={form.productName} onChange={e => set('productName', e.target.value)} />
            {errors.productName && <p className={errorClass}>{errors.productName}</p>}
          </div>
          <div>
            <label className={labelClass}>Product URL *</label>
            <input className={inputClass} type="url" placeholder="https://example.com/product" value={form.productUrl} onChange={e => set('productUrl', e.target.value)} />
            {errors.productUrl && <p className={errorClass}>{errors.productUrl}</p>}
          </div>
          <div>
            <label className={labelClass}>Product Image URL *</label>
            <input className={inputClass} type="url" placeholder="https://example.com/image.jpg" value={form.productImageUrl} onChange={e => set('productImageUrl', e.target.value)} />
            {errors.productImageUrl && <p className={errorClass}>{errors.productImageUrl}</p>}
          </div>
        </div>
      </div>

      <CampaignPricingFields form={form} errors={errors} set={set} />
    </section>
  )
}
