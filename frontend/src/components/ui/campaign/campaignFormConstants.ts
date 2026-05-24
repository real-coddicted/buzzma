import type { Platform, CampaignType, LinkedEntity } from '../../../types'

export const EMPTY_FORM = {
  title: '',
  platform: '' as Platform | '',
  productBrandName: '',
  productName: '',
  productImageUrl: '',
  productUrl: '',
  sellerName: '',
  originalPriceRupees: '',
  campaignPriceRupees: '',
  commissionRupees: '',
  returnWindowDays: '',
  campaignType: '' as CampaignType | '',
  startDate: '',
  endDate: '',
  totalSlots: '',
  openToAll: false,
  assignees: [] as LinkedEntity[],
  termsAndConditions: '',
}

export const labelClass =
  'block text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-1'

export const inputClass = [
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover',
  'border-surface-light-border dark:border-surface-dark-border',
  'text-xs text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'px-3 py-2 outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all',
].join(' ')

export const errorClass = 'text-[10px] text-neon-red mt-1'

export type CampaignForm = typeof EMPTY_FORM

export function validateCampaignForm(form: CampaignForm): Partial<Record<string, string>> {
  const e: Partial<Record<string, string>> = {}

  if (!form.title.trim()) e.title = 'Required'
  if (!form.platform) e.platform = 'Required'
  if (!form.productBrandName.trim()) e.productBrandName = 'Required'
  if (!form.productName.trim()) e.productName = 'Required'
  if (!form.productImageUrl.trim()) e.productImageUrl = 'Required'
  if (!form.productUrl.trim()) e.productUrl = 'Required'

  if (!form.startDate) e.startDate = 'Required'
  if (!form.endDate) e.endDate = 'Required'
  if (form.startDate && form.endDate && form.endDate < form.startDate)
    e.endDate = 'End date must be after start date'

  const orig = parseFloat(form.originalPriceRupees)
  if (isNaN(orig) || orig < 0) e.originalPriceRupees = 'Enter a valid amount'
  const camp = parseFloat(form.campaignPriceRupees)
  if (isNaN(camp) || camp < 0) e.campaignPriceRupees = 'Enter a valid amount'

  if (form.returnWindowDays !== '') {
    const rw = parseInt(form.returnWindowDays, 10)
    if (isNaN(rw) || rw < 0) e.returnWindowDays = 'Must be a non-negative integer'
  }
  if (form.totalSlots !== '') {
    const ts = parseInt(form.totalSlots, 10)
    if (isNaN(ts) || ts < 1) e.totalSlots = 'Must be a positive integer'
  }

  return e
}

