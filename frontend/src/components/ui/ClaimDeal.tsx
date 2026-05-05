import { useState } from 'react'
import type { Deal, Platform } from '../../types/DealTypes'

const platformLabel: Record<Platform, string> = {
  PLATFORM_AMAZON:   'Amazon',
  PLATFORM_FLIPKART: 'Flipkart',
  PLATFORM_NYKAA:    'Nykaa',
  PLATFORM_MYNTRA:   'Myntra',
}

interface ClaimDealProps {
  deal: Deal
}

export function ClaimDeal({ deal }: ClaimDealProps) {
  const [orderId, setOrderId] = useState('')

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 space-y-5">
      <h3 className="text-base font-bold text-ink-light-primary dark:text-ink-dark-primary">
        Claim Deal
      </h3>

      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted leading-relaxed">
        Purchase this product on {platformLabel[deal.platform]} at the offered price and submit your order details to claim the deal.
      </p>

      <div className="space-y-3">
        <div>
          <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
            Order ID
          </label>
          <input
            type="text"
            placeholder="Enter your order ID"
            value={orderId}
            onChange={e => setOrderId(e.target.value)}
            className="w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary px-3 py-2 outline-none focus:border-neon-blue/50 transition-colors placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
            Screenshot (optional)
          </label>
          <div className="w-full rounded-lg border-2 border-dashed border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/40 transition-colors px-4 py-6 flex flex-col items-center gap-2 cursor-pointer">
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              Drop file here or click to upload
            </span>
          </div>
        </div>
      </div>

      <button
        disabled={!orderId.trim()}
        className="w-full py-2.5 rounded-lg bg-neon-blue text-surface-dark-base text-sm font-semibold hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
      >
        Submit Claim
      </button>
    </div>
  )
}
