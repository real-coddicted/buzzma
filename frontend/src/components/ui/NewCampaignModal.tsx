import { useState, useEffect, type FormEvent, type KeyboardEvent } from 'react'
import { Button } from './Button'
import { IconPlus } from './icons'
import type { Platform, CampaignType, CampaignRequestDto } from '../../types'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../../constants/campaigns'

export type { CampaignRequestDto }

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (dto: CampaignRequestDto) => void
}


const EMPTY_FORM = {
  title: '',
  platform: '' as Platform | '',
  productBrandName: '',
  productImageUrl: '',
  productUrl: '',
  originalPriceRupees: '',
  campaignPriceRupees: '',
  commissionRupees: '',
  returnWindowDays: '',
  campaignType: '' as CampaignType | '',
  totalSlots: '',
  openToAll: false,
  agencyInput: '',
  allowedAgencies: [] as string[],
}

const labelClass =
  'block text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-1'

const inputClass = [
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover',
  'border-surface-light-border dark:border-surface-dark-border',
  'text-xs text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'px-3 py-2 outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all',
].join(' ')

const errorClass = 'text-[10px] text-neon-red mt-1'

function rupeesToPaise(val: string): number {
  return Math.round(parseFloat(val) * 100)
}

export function NewCampaignModal({ open, onClose, onSubmit }: Props) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [errors, setErrors] = useState<Partial<Record<string, string>>>({})
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open) {
      setForm(EMPTY_FORM)
      setErrors({})
      setLoading(false)
    }
  }, [open])

  useEffect(() => {
    function onKey(e: globalThis.KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    if (open) document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onClose])

  if (!open) return null

  function set(field: keyof typeof EMPTY_FORM, value: unknown) {
    setForm(prev => ({ ...prev, [field]: value }))
    setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  function validate(): boolean {
    const e: Partial<Record<string, string>> = {}

    if (!form.title.trim()) e.title = 'Required'
    if (!form.platform) e.platform = 'Required'
    if (!form.productBrandName.trim()) e.productBrandName = 'Required'
    if (!form.productImageUrl.trim()) e.productImageUrl = 'Required'
    if (!form.productUrl.trim()) e.productUrl = 'Required'

    const orig = parseFloat(form.originalPriceRupees)
    if (isNaN(orig) || orig < 0) e.originalPriceRupees = 'Enter a valid amount'
    const camp = parseFloat(form.campaignPriceRupees)
    if (isNaN(camp) || camp < 0) e.campaignPriceRupees = 'Enter a valid amount'
    const comm = parseFloat(form.commissionRupees)
    if (isNaN(comm) || comm < 0) e.commissionRupees = 'Enter a valid amount'

    if (form.returnWindowDays !== '') {
      const rw = parseInt(form.returnWindowDays, 10)
      if (isNaN(rw) || rw < 0) e.returnWindowDays = 'Must be a non-negative integer'
    }
    if (form.totalSlots !== '') {
      const ts = parseInt(form.totalSlots, 10)
      if (isNaN(ts) || ts < 1) e.totalSlots = 'Must be a positive integer'
    }

    setErrors(e)
    return Object.keys(e).length === 0
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)

    const dto: CampaignRequestDto = {
      title: form.title.trim(),
      platform: form.platform,
      productBrandName: form.productBrandName.trim(),
      productImageUrl: form.productImageUrl.trim(),
      productUrl: form.productUrl.trim(),
      originalPricePaise: rupeesToPaise(form.originalPriceRupees),
      campaignPricePaise: rupeesToPaise(form.campaignPriceRupees),
      commissionOfferedPaise: rupeesToPaise(form.commissionRupees),
      returnWindowDays: form.returnWindowDays !== '' ? parseInt(form.returnWindowDays, 10) : null,
      campaignType: form.campaignType !== '' ? form.campaignType : null,
      totalSlots: form.totalSlots !== '' ? parseInt(form.totalSlots, 10) : null,
      allowedAgencies: form.openToAll ? null : form.allowedAgencies.length > 0 ? form.allowedAgencies : null,
      openToAll: form.openToAll,
    }

    onSubmit(dto)
    setLoading(false)
  }

  function addAgency() {
    const val = form.agencyInput.trim()
    if (!val || form.allowedAgencies.includes(val)) return
    set('allowedAgencies', [...form.allowedAgencies, val])
    setForm(prev => ({ ...prev, agencyInput: '' }))
  }

  function removeAgency(agency: string) {
    set('allowedAgencies', form.allowedAgencies.filter(a => a !== agency))
  }

  function onAgencyKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') {
      e.preventDefault()
      addAgency()
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      aria-modal="true"
      role="dialog"
      aria-labelledby="modal-title"
    >
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Panel */}
      <div className="relative w-full max-w-2xl max-h-[90vh] flex flex-col rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-surface-light-border dark:border-surface-dark-border">
          <div>
            <h2 id="modal-title" className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">
              New Campaign
            </h2>
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
              Fill in the details to create a new campaign.
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
            aria-label="Close"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <form id="new-campaign-form" onSubmit={handleSubmit} noValidate className="overflow-y-auto flex-1 px-6 py-5 space-y-6">
          {/* Basic Info */}
          <section className="space-y-4">
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
                <select
                  className={inputClass}
                  value={form.campaignType}
                  onChange={e => set('campaignType', e.target.value as CampaignType | '')}
                >
                  <option value="">— None —</option>
                  {(Object.keys(CAMPAIGN_TYPE_LABELS) as CampaignType[]).map(k => (
                    <option key={k} value={k}>{CAMPAIGN_TYPE_LABELS[k]}</option>
                  ))}
                </select>
              </div>
            </div>
          </section>

          {/* Product */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-cyan">Product</h3>
            <div>
              <label className={labelClass}>Brand Name *</label>
              <input className={inputClass} type="text" placeholder="e.g. Acme Co." value={form.productBrandName} onChange={e => set('productBrandName', e.target.value)} />
              {errors.productBrandName && <p className={errorClass}>{errors.productBrandName}</p>}
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
          </section>

          {/* Pricing */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-green">Pricing (₹)</h3>
            <div className="grid grid-cols-3 gap-4">
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
              <div>
                <label className={labelClass}>Commission *</label>
                <div className="relative">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs text-ink-light-muted dark:text-ink-dark-muted">₹</span>
                  <input className={[inputClass, 'pl-6'].join(' ')} type="number" min="0" step="0.01" placeholder="0.00" value={form.commissionRupees} onChange={e => set('commissionRupees', e.target.value)} />
                </div>
                {errors.commissionRupees && <p className={errorClass}>{errors.commissionRupees}</p>}
              </div>
            </div>
          </section>

          {/* Settings */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-orange">Campaign Settings</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Total Slots</label>
                <input className={inputClass} type="number" min="1" step="1" placeholder="e.g. 100" value={form.totalSlots} onChange={e => set('totalSlots', e.target.value)} />
                {errors.totalSlots && <p className={errorClass}>{errors.totalSlots}</p>}
              </div>
              <div>
                <label className={labelClass}>Return Window (days)</label>
                <input className={inputClass} type="number" min="0" step="1" placeholder="e.g. 30" value={form.returnWindowDays} onChange={e => set('returnWindowDays', e.target.value)} />
                {errors.returnWindowDays && <p className={errorClass}>{errors.returnWindowDays}</p>}
              </div>
            </div>

            {/* Open to All toggle */}
            <div className="flex items-center gap-3">
              <button
                type="button"
                role="switch"
                aria-checked={form.openToAll}
                onClick={() => set('openToAll', !form.openToAll)}
                className={[
                  'relative inline-flex h-5 w-9 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-neon-blue/40',
                  form.openToAll ? 'bg-neon-blue' : 'bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border',
                ].join(' ')}
              >
                <span
                  className={[
                    'pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200',
                    form.openToAll ? 'translate-x-4' : 'translate-x-0',
                  ].join(' ')}
                />
              </button>
              <span className="text-xs text-ink-light-primary dark:text-ink-dark-primary font-medium">Open to All</span>
              <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                {form.openToAll ? 'Any agency can participate' : 'Restricted to allowed agencies'}
              </span>
            </div>

            {/* Allowed Agencies — shown when not open to all */}
            {!form.openToAll && (
              <div>
                <label className={labelClass}>Allowed Agencies</label>
                <div className="flex gap-2">
                  <input
                    className={[inputClass, 'flex-1'].join(' ')}
                    type="text"
                    placeholder="Agency name — press Enter to add"
                    value={form.agencyInput}
                    onChange={e => setForm(prev => ({ ...prev, agencyInput: e.target.value }))}
                    onKeyDown={onAgencyKeyDown}
                  />
                  <Button type="button" variant="secondary" size="sm" leftIcon={<IconPlus size={12} />} onClick={addAgency}>
                    Add
                  </Button>
                </div>
                {form.allowedAgencies.length > 0 && (
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {form.allowedAgencies.map(a => (
                      <span
                        key={a}
                        className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-neon-purple/10 text-neon-purple border border-neon-purple/25"
                      >
                        {a}
                        <button
                          type="button"
                          onClick={() => removeAgency(a)}
                          className="hover:text-neon-red transition-colors leading-none"
                          aria-label={`Remove ${a}`}
                        >
                          ×
                        </button>
                      </span>
                    ))}
                  </div>
                )}
              </div>
            )}
          </section>
        </form>

        {/* Footer */}
        <div className="flex items-center justify-end gap-2 px-6 py-4 border-t border-surface-light-border dark:border-surface-dark-border">
          <Button variant="secondary" size="sm" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            type="submit"
            form="new-campaign-form"
            variant="primary"
            size="sm"
            leftIcon={<IconPlus size={13} />}
            loading={loading}
          >
            Create Campaign
          </Button>
        </div>
      </div>
    </div>
  )
}

interface NewCampaignButtonProps {
  onSubmit?: (dto: CampaignRequestDto) => void
  size?: 'sm' | 'md' | 'lg'
}

export function NewCampaignButton({ onSubmit, size = 'md' }: NewCampaignButtonProps) {
  const [open, setOpen] = useState(false)

  function handleSubmit(dto: CampaignRequestDto) {
    onSubmit?.(dto)
    setOpen(false)
  }

  return (
    <>
      <Button variant="primary" size={size} leftIcon={<IconPlus size={14} />} onClick={() => setOpen(true)}>
        New Campaign
      </Button>
      <NewCampaignModal open={open} onClose={() => setOpen(false)} onSubmit={handleSubmit} />
    </>
  )
}
