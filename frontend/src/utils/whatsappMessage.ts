import type { Deal } from '../types/DealTypes'
import { paiseToRupees, formatRupees } from './currency'

export function buildWhatsAppMessage(deal: Deal, mediatorName: string): string {
  const original = paiseToRupees(deal.originalPricePaise)
  const offered = paiseToRupees(deal.offeredPricePaise)
  const noteLines = (deal.termsAndConditions ?? '')
    .split('\n')
    .map(l => l.trim())
    .filter(Boolean)

  const lines = [
    '🚨 *Order in next 10 Minutes, Else slot will be passed!* 🚨',
    '',
    `📦 Product: ${deal.productName}`,
    `🏷️ Deal Type: ${deal.dealTypeLabel}`,
    `🔢 Deal Code: ${deal.code}`,
    `🛒 Platform: ${deal.platformLabel}`,
    '',
    '🔗 Link:',
    deal.productUrl,
    '',
    `👤 Mediator Name: ${mediatorName}`,
    `💰 Original Price: ₹${formatRupees(original)}/-`,
    `💰 Offer Price: ₹${formatRupees(offered)}/-`,
  ]

  if (noteLines.length > 0) {
    lines.push('', '📌 Note:', ...noteLines)
  }

  lines.push('', '🙏 Thank You!')

  return lines.join('\n')
}
