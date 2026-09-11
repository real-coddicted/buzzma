import type { Platform, CampaignType, LinkedEntity, PromotionCategory } from '../../../types'

/**
 * One row in the campaign-creation "Exchange Products" section.
 * `prefilled` rows come from the agency master list or from products already persisted on the
 * campaign — their name is locked; the image URL stays editable.
 */
export interface ExchangeProductRow {
  productName: string
  productImageUrl: string
  selected: boolean
  prefilled: boolean
}

export const EMPTY_FORM = {
  title: '',
  category: 'ECOMMERCE' as PromotionCategory,
  platform: '' as Platform | '',
  productBrandName: '',
  productName: '',
  productImageUrl: '',
  productUrl: '',
  sellerName: '',
  originalPriceRupees: '',
  campaignPriceRupees: '',
  commissionToAllRupees: '',
  returnWindowDays: '',
  campaignType: '' as CampaignType | '',
  startDate: '',
  endDate: '',
  totalSlots: '',
  openToAll: false,
  affiliateLinkAllowed: false,
  assignees: [] as LinkedEntity[],
  termsAndConditions: '',
  requiredSteps: ['ORDER'] as string[],
  exchangeProducts: [] as ExchangeProductRow[],
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

/** Lenient client-side image-URL check: empty is allowed; otherwise it must parse and be http(s). */
export function isValidImageUrl(value: string): boolean {
  if (!value.trim()) return true
  try {
    const { protocol } = new URL(value)
    return protocol === 'http:' || protocol === 'https:'
  } catch {
    return false
  }
}

/**
 * Builds the exchange-product rows shown on the campaign form from the agency master list and any
 * rows already saved on the campaign. Master entries become locked rows (unchecked unless a saved
 * row matches by name, case-insensitively — the backend dedupes names the same way); saved rows
 * with no matching master entry are kept as trailing locked rows. The blank "add" row is appended
 * by the component, not here.
 */
export function mergeExchangeProductRows(
  master: { name?: string }[],
  saved: ExchangeProductRow[],
): ExchangeProductRow[] {
  const savedByName = new Map(saved.map(r => [r.productName.trim().toLowerCase(), r]))
  const rows: ExchangeProductRow[] = master.map(m => {
    const productName = (m.name ?? '').trim()
    const match = savedByName.get(productName.toLowerCase())
    savedByName.delete(productName.toLowerCase())
    return {
      productName,
      productImageUrl: match?.productImageUrl ?? '',
      selected: !!match,
      prefilled: true,
    }
  })
  for (const leftover of savedByName.values()) {
    rows.push({ ...leftover, prefilled: true })
  }
  return rows
}

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
  if (form.endDate && form.endDate < new Date().toISOString().slice(0, 10))
    e.endDate = 'End date cannot be in the past'

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
    if (isNaN(ts) || ts < 1) {
      e.totalSlots = 'Must be a positive integer'
    } else if (!form.openToAll && form.assignees && form.assignees.length > 0) {
      const assignedSlots = form.assignees.reduce((sum, item) => sum + (item.slotsAvailable || 0), 0)
      if (assignedSlots > ts) {
        e.assignedSlots = `Total assigned slots (${assignedSlots}) cannot exceed campaign total slots (${ts})`
      }
    }
  }

  if (form.campaignType === 'CAMPAIGN_TYPE_EXCHANGE') {
    const chosen = form.exchangeProducts.filter(r => r.selected && r.productName.trim())
    if (chosen.length === 0) {
      e.exchangeProducts = 'Select at least one exchange product with a name'
    } else if (chosen.some(r => !isValidImageUrl(r.productImageUrl))) {
      e.exchangeProducts = 'Enter a valid http(s) image URL for each selected product, or leave it blank'
    }
  }

  return e
}

