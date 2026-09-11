import type { Platform, CampaignType, PromotionCategory } from '../../../types'
import { PLATFORM_LABELS, APP_STORE_PLATFORMS, PROMOTION_CATEGORY_PLATFORMS } from '../../../constants/campaigns'
import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { RupeeInput } from '../RupeeInput'

interface FormSlice {
  category: PromotionCategory
  platform: Platform | ''
  campaignType: CampaignType | ''
  productBrandName: string
  productName: string
  productUrl: string
  productImageUrl: string
  sellerName: string
  originalPriceRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: unknown) => void
  readOnly?: boolean
}

export function CampaignProductFields({ form, errors, set, readOnly }: Props) {
  function onPlatformChange(value: Platform | '') {
    set('platform', value)
    const nowAppStore = APP_STORE_PLATFORMS.includes(value as Platform)
    if (nowAppStore && form.campaignType !== 'CAMPAIGN_TYPE_APP_REVIEW') set('campaignType', 'CAMPAIGN_TYPE_APP_REVIEW')
    if (!nowAppStore && form.campaignType === 'CAMPAIGN_TYPE_APP_REVIEW') set('campaignType', '')
  }

  return (
    <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-cyan">Product</h3>
      <div className="space-y-4">
        <div>
          <label className={labelClass}>Platform *</label>
          <select className={inputClass} value={form.platform} onChange={e => onPlatformChange(e.target.value as Platform | '')} disabled={readOnly}>
            <option value="">— Select —</option>
            {PROMOTION_CATEGORY_PLATFORMS[form.category].map(k => (
              <option key={k} value={k}>{PLATFORM_LABELS[k]}</option>
            ))}
          </select>
          {errors.platform && <p className={errorClass}>{errors.platform}</p>}
        </div>
        <div>
          <label className={labelClass}>Brand Name *</label>
          <input className={inputClass} type="text" placeholder="e.g. Acme Co." value={form.productBrandName} onChange={e => set('productBrandName', e.target.value)} disabled={readOnly} />
          {errors.productBrandName && <p className={errorClass}>{errors.productBrandName}</p>}
        </div>
        <div>
          <label className={labelClass}>Seller Name</label>
          <input className={inputClass} type="text" placeholder="e.g. Acme Store" value={form.sellerName} onChange={e => set('sellerName', e.target.value)} disabled={readOnly} />
        </div>
        <div>
          <label className={labelClass}>Product Name *</label>
          <input className={inputClass} type="text" placeholder="e.g. Acme Coffee Cup" value={form.productName} onChange={e => set('productName', e.target.value)} disabled={readOnly} />
          {errors.productName && <p className={errorClass}>{errors.productName}</p>}
        </div>
        <div>
          <label className={labelClass}>Product URL *</label>
          <input className={inputClass} type="url" placeholder="https://example.com/product" value={form.productUrl} onChange={e => set('productUrl', e.target.value)} disabled={readOnly} />
          {errors.productUrl && <p className={errorClass}>{errors.productUrl}</p>}
        </div>
        <div>
          <label className={labelClass}>Product Image URL *</label>
          <input className={inputClass} type="url" placeholder="https://example.com/image.jpg" value={form.productImageUrl} onChange={e => set('productImageUrl', e.target.value)} disabled={readOnly} />
          {errors.productImageUrl && <p className={errorClass}>{errors.productImageUrl}</p>}
        </div>
        <div>
          <label className={labelClass}>Original Price *</label>
          <RupeeInput value={form.originalPriceRupees} onChange={v => set('originalPriceRupees', v)} className={inputClass} disabled={readOnly} />
          {errors.originalPriceRupees && <p className={errorClass}>{errors.originalPriceRupees}</p>}
        </div>
      </div>
    </section>
  )
}
