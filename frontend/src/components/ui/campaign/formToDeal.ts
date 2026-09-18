import type { Deal, Platform, CampaignType } from '../../../types/DealTypes'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../../../constants/campaigns'
import { rupeesToPaise } from '../../../utils/currency'
import type { CampaignForm } from './campaignFormConstants'

/** Primary image first, then each selected exchange product's image, dropping blanks. */
function toProductImages(primary: string, exchangeProducts: CampaignForm['exchangeProducts']): string[] {
  return [primary, ...exchangeProducts.filter(p => p.selected).map(p => p.productImageUrl)].filter(Boolean)
}

/** Builds a preview-only Deal from the in-progress campaign form, before it's saved. No API calls involved. */
export function campaignFormToDeal(form: CampaignForm): Deal {
  const platform = form.platform as Platform
  const dealType = form.campaignType as CampaignType
  const totalSlots = form.totalSlots !== '' ? parseInt(form.totalSlots, 10) : NaN

  return {
    id: '',
    campaignId: '',
    title: form.title.trim(),
    productName: form.productName.trim(),
    productImageUrl: form.productImageUrl.trim(),
    productImages: toProductImages(form.productImageUrl.trim(), form.exchangeProducts),
    productUrl: form.productUrl.trim(),
    platform,
    platformLabel: PLATFORM_LABELS[platform] ?? platform,
    dealType,
    dealTypeLabel: CAMPAIGN_TYPE_LABELS[dealType] ?? dealType,
    originalPricePaise: rupeesToPaise(parseFloat(form.originalPriceRupees) || 0),
    offeredPricePaise: rupeesToPaise(parseFloat(form.campaignPriceRupees) || 0),
    sellerName: form.sellerName.trim() || undefined,
    termsAndConditions: form.termsAndConditions.trim() || undefined,
    startDate: form.startDate || undefined,
    endDate: form.endDate || undefined,
    slotsAvailable: isNaN(totalSlots) ? undefined : totalSlots,
    requiredSteps: form.requiredSteps,
    status: 'explore',
  }
}
