import { PLATFORM_LABELS } from '../../../constants/campaigns'
import { paiseToRupees, formatRupees } from '../../../utils/currency'
import type { Platform, Deal, ClaimReviewItem } from '../../../types'
import type { ClaimProofItem } from './ClaimProofGallery'

export function formatExtractedValue(key: string, raw: string): string {
  if (key === 'platform') return PLATFORM_LABELS[raw as Platform] ?? raw
  if (key === 'amount') {
    const n = parseFloat(raw)
    if (!isNaN(n)) return `₹${formatRupees(n)}`
  }
  return raw
}

export function getCampaignValue(key: string, deal: Deal): string | undefined {
  switch (key) {
    case 'platform':    return deal.platformLabel
    case 'productName': return deal.productName
    case 'sellerName':  return deal.sellerName ?? undefined
    case 'amount':      return deal.originalPricePaise != null
      ? `₹${formatRupees(paiseToRupees(deal.originalPricePaise))}`
      : undefined
    default: return undefined
  }
}

export function getSubmittedValue(key: string, claim: ClaimReviewItem): string | undefined {
  switch (key) {
    case 'orderId':      return claim.orderId
    case 'orderDate':    return claim.orderDate
      ? claim.orderDate.replace(/^(\d{4})(\d{2})(\d{2})$/, '$1-$2-$3')
      : undefined
    case 'productName':  return claim.productName ?? undefined
    case 'sellerName':   return claim.sellerName ?? undefined
    case 'accountName':
    case 'buyerName':    return claim.accountName ?? undefined
    case 'amount':       return claim.amountPaise != null
      ? `₹${formatRupees(paiseToRupees(claim.amountPaise))}`
      : undefined
    case 'reviewUrl':    return claim.reviewUrl ?? undefined
    default: return undefined
  }
}

export function getProofScore(item: ClaimProofItem): number {
  if (item.score != null) return Math.round(item.score * 100)
  if (item.fields.length === 0) return 0
  return Math.round((item.fields.filter(f => f.matched).length / item.fields.length) * 100)
}

export function scorePillClass(pct: number): string {
  if (pct >= 80) return 'bg-neon-green/10 border border-neon-green/30 text-neon-green'
  if (pct >= 50) return 'bg-neon-yellow/10 border border-neon-yellow/30 text-neon-yellow'
  return 'bg-neon-red/10 border border-neon-red/30 text-neon-red'
}
