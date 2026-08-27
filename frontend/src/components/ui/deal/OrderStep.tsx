import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { paiseToRupees } from '../../../utils/currency'
import { useRejectedScreenshotUrl } from '../../../hooks/useRejectedScreenshotUrl'
import { DealOrderForm } from './DealOrderForm'
import { ScreenshotPreview } from './ScreenshotPreview'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface OrderStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function OrderStep({ deal, claimId, onSuccess, readOnly = false, claimResponse, rejectedScreenshot }: OrderStepProps) {
  const screenshotUrl = useRejectedScreenshotUrl(rejectedScreenshot?.storageKey)

  const orderScreenshotKey = rejectedScreenshot?.storageKey
    ?? claimResponse?.screenshots?.find(s => s.type === 'SCREENSHOT_TYPE_ORDER')?.storageKey

  const claimValues = claimResponse ? {
    platform:    String(claimResponse.platform ?? ''),
    orderId:     claimResponse.ecommerceOrderId ?? '',
    amount:      claimResponse.amountPaise != null ? String(paiseToRupees(claimResponse.amountPaise)) : '',
    productName: claimResponse.productName ?? '',
    sellerName:  claimResponse.sellerName ?? '',
    orderDate:   claimResponse.orderDate != null
      ? String(claimResponse.orderDate).replace(/^(\d{4})(\d{2})(\d{2})$/, '$1-$2-$3')
      : '',
    accountName: claimResponse.accountName ?? '',
  } : undefined

  if (rejectedScreenshot) {
    return (
      <div className="space-y-5">

        <DealOrderForm
          dealId={deal.id}
          campaignId={deal.campaignId}
          onSuccess={onSuccess}
          claimValues={claimValues}
          sellerNameOptional={!deal.sellerName}
          resubmit={{
            claimId: claimId!,
            screenshotId: rejectedScreenshot.id ?? '',
            initialScreenshotUrl: screenshotUrl ?? undefined,
          }}
        />
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {!readOnly && (
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
          Purchase this product on{' '}
          <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            {deal.platformLabel}
          </span>{' '}
          at the offered price, then upload the screenshot of your order confirmation and fill in
          your order details below.
        </p>
      )}
      {readOnly && <ScreenshotPreview storageKey={orderScreenshotKey} label="Order Screenshot" />}
      <DealOrderForm
        dealId={deal.id}
        campaignId={deal.campaignId}
        onSuccess={onSuccess}
        readOnly={readOnly}
        claimValues={readOnly ? claimValues : undefined}
        sellerNameOptional={!deal.sellerName}
      />
    </div>
  )
}
