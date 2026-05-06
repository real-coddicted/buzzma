import type { Deal } from '../../../types/DealTypes'

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between items-center py-3 border-b border-surface-light-border dark:border-surface-dark-border last:border-0">
      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{label}</span>
      <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">{value}</span>
    </div>
  )
}

interface ClaimStepOrderPlacedProps {
  deal: Deal
}

export function ClaimStepOrderPlaced({ deal }: ClaimStepOrderPlacedProps) {
  const order = deal.claimOrder

  if (!order) {
    return <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">No order details found.</p>
  }

  return (
    <div className="space-y-4">
      <h4 className="text-xs font-bold uppercase tracking-wider text-ink-light-primary dark:text-ink-dark-primary">
        Order Details
      </h4>

      <div>
        <Row label="Order ID"   value={order.orderId}     />
        <Row label="Amount"     value={order.amount}      />
        <Row label="Product"    value={order.productName} />
        <Row label="Sold By"    value={order.soldBy}      />
        <Row label="Order Date" value={order.orderDate}   />
        <Row label="Account"    value={order.accountName} />
      </div>

      {order.screenshotUrl && (
        <div className="space-y-2">
          <p className="text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary">
            Order Confirmation Screenshot
          </p>
          <img
            src={order.screenshotUrl}
            alt="Order confirmation"
            className="w-full rounded-xl object-cover border border-surface-light-border dark:border-surface-dark-border"
          />
        </div>
      )}
    </div>
  )
}
