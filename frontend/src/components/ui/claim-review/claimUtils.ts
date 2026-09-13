import { PLATFORM_LABELS } from '../../../constants/campaigns'
import { paiseToRupees, formatRupees } from '../../../utils/currency'
import type { Platform, Deal, ClaimReviewItem, ClaimStatus, ScreenshotVerificationStatus } from '../../../types'
import type { ClaimProofItem } from './ClaimProofGallery'

/**
 * Agencies and brands can review claims — reviewing individual screenshots and (agency-only)
 * approving/rejecting the claim itself. Brands are scoped server-side to campaigns shared with
 * them.
 */
export function canReviewClaims(userRole: string | undefined): boolean {
  return userRole === 'ROLE_AGENCY' || userRole === 'ROLE_BRAND'
}

/**
 * Agencies hold sole final-decision authority: only they approve or reject a claim and set the
 * approved amount. Brands can only verify (their own sign-off), never approve or reject.
 */
export function canApproveClaims(userRole: string | undefined): boolean {
  return userRole === 'ROLE_AGENCY'
}

/**
 * A claim reaches one of these statuses once it has been decided and moved to accounting or
 * beyond — neither claim review nor screenshot review can touch it anymore for either role.
 */
export function isClaimLocked(status: ClaimStatus | undefined): boolean {
  return (
    status === 'APPROVED' ||
    status === 'REJECTED' ||
    status === 'READY_FOR_ACCOUNTING' ||
    status === 'REWARD_PENDING' ||
    status === 'COMPLETED' ||
    status === 'FAILED'
  )
}

/**
 * Reset Review is available to both Agency and Brand, but only while the claim is still
 * APPROVED — rejected claims are never reset, and once the claim moves further (ready for
 * accounting or beyond) it is locked for both roles.
 */
export function canResetClaim(userRole: string | undefined, claimStatus: ClaimStatus): boolean {
  return canReviewClaims(userRole) && claimStatus === 'APPROVED'
}

/**
 * The screenshot review Submit button is a no-op guard: disabled once the claim itself is
 * locked, or when the selected status already matches what's stored on the server.
 */
export function isReviewSubmitDisabled(
  claimLocked: boolean,
  selectedStatus: ScreenshotVerificationStatus,
  serverStatus: ScreenshotVerificationStatus,
): boolean {
  return claimLocked || selectedStatus === serverStatus
}

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
    case 'platform':     return claim.platform ? PLATFORM_LABELS[claim.platform] : undefined
    case 'orderId':      return claim.orderId
    case 'orderDate':    return claim.orderDate
      ? claim.orderDate.replace(/^(\d{4})(\d{2})(\d{2})$/, '$1-$2-$3')
      : undefined
    case 'productName':  return claim.productName ?? undefined
    case 'sellerName':   return claim.sellerName ?? undefined
    case 'accountName':
    case 'buyerName':    return claim.accountName ?? undefined
    case 'orderedBy':    return claim.orderedBy ?? undefined
    case 'amount':       return claim.amountPaise != null
      ? `₹${formatRupees(paiseToRupees(claim.amountPaise))}`
      : undefined
    case 'reviewUrl':    return claim.reviewUrl ?? undefined
    default: return undefined
  }
}

export function getProofScore(item: ClaimProofItem): number {
  if (item.score != null) return item.score
  if (item.fields.length === 0) return 0
  return Math.round((item.fields.filter(f => f.matched).length / item.fields.length) * 100)
}

export function scorePillClass(pct: number): string {
  if (pct >= 95) return 'bg-neon-green/10 border border-neon-green/30 text-neon-green'
  if (pct >= 50) return 'bg-neon-yellow/10 border border-neon-yellow/30 text-neon-yellow'
  return 'bg-neon-red/10 border border-neon-red/30 text-neon-red'
}
