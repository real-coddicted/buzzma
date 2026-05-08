import { useEffect } from 'react'
import { Button } from '../Button'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../../../constants/campaigns'
import type { Campaign, Platform } from '../../../types'

interface Props {
  open: boolean
  campaign: Campaign | null
  onClose: () => void
}

const labelClass =
  'block text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-1'

const readOnlyInputClass = [
  'w-full rounded-lg border bg-surface-light-hover/50 dark:bg-surface-dark-hover/50',
  'border-surface-light-border dark:border-surface-dark-border',
  'text-xs text-ink-light-primary dark:text-ink-dark-primary',
  'px-3 py-2 outline-none cursor-not-allowed',
].join(' ')

function paiseToRupees(paise: number): string {
  return (paise / 100).toFixed(2)
}

export function CampaignDetailsModal({ open, campaign, onClose }: Props) {
  useEffect(() => {
    function onKey(e: globalThis.KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    if (open) document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onClose])

  if (!open || !campaign) return null

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
              Campaign Details
            </h2>
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
              View campaign information (read-only)
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
        <div className="overflow-y-auto flex-1 px-6 py-5 space-y-6">
          {/* Campaign Overview */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-blue">Campaign Overview</h3>
            <div>
              <label className={labelClass}>Campaign Title</label>
              <input className={readOnlyInputClass} type="text" value={campaign.title} readOnly disabled />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Status</label>
                <input className={readOnlyInputClass} type="text" value={campaign.status.charAt(0).toUpperCase() + campaign.status.slice(1)} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Platform</label>
                <input className={readOnlyInputClass} type="text" value={PLATFORM_LABELS[campaign.platform]} readOnly disabled />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Start Date</label>
                <input className={readOnlyInputClass} type="text" value={campaign.startDate} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>End Date</label>
                <input className={readOnlyInputClass} type="text" value={campaign.endDate} readOnly disabled />
              </div>
            </div>
          </section>

          {/* Product Information */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-cyan">Product Information</h3>
            <div>
              <label className={labelClass}>Brand Name</label>
              <input className={readOnlyInputClass} type="text" value={campaign.productBrandName} readOnly disabled />
            </div>
            <div>
              <label className={labelClass}>Product URL</label>
              <input className={readOnlyInputClass} type="url" value={campaign.productUrl} readOnly disabled />
            </div>
            <div>
              <label className={labelClass}>Product Image URL</label>
              <input className={readOnlyInputClass} type="url" value={campaign.productImageUrl} readOnly disabled />
            </div>
          </section>

          {/* Pricing Information */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-green">Pricing Information (₹)</h3>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className={labelClass}>Original Price</label>
                <input className={readOnlyInputClass} type="text" value={paiseToRupees(campaign.originalPricePaise)} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Campaign Price</label>
                <input className={readOnlyInputClass} type="text" value={paiseToRupees(campaign.campaignPricePaise)} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Commission</label>
                <input className={readOnlyInputClass} type="text" value={paiseToRupees(campaign.commissionOfferedPaise)} readOnly disabled />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Total Slots</label>
                <input className={readOnlyInputClass} type="text" value={campaign.totalSlots || '—'} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Return Window (days)</label>
                <input className={readOnlyInputClass} type="text" value={campaign.returnWindowDays || '—'} readOnly disabled />
              </div>
            </div>
          </section>

          {/* Campaign Settings */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-orange">Campaign Settings</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Campaign Type</label>
                <input className={readOnlyInputClass} type="text" value={campaign.campaignType ? CAMPAIGN_TYPE_LABELS[campaign.campaignType] : '—'} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Open to All</label>
                <input className={readOnlyInputClass} type="text" value={campaign.openToAll ? 'Yes' : 'No'} readOnly disabled />
              </div>
            </div>
            {campaign.allowedAgencies && campaign.allowedAgencies.length > 0 && (
              <div>
                <label className={labelClass}>Allowed Agencies</label>
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {campaign.allowedAgencies.map((agency, idx) => (
                    <span
                      key={idx}
                      className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium bg-neon-purple/10 text-neon-purple border border-neon-purple/25"
                    >
                      {agency}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </section>

          {/* Performance Metrics */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-purple">Performance Metrics</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Impressions</label>
                <input
                  className={readOnlyInputClass}
                  type="text"
                  value={
                    campaign.impressions >= 1_000_000
                      ? `${(campaign.impressions / 1_000_000).toFixed(1)}M`
                      : campaign.impressions >= 1_000
                      ? `${(campaign.impressions / 1_000).toFixed(0)}K`
                      : campaign.impressions.toString()
                  }
                  readOnly
                  disabled
                />
              </div>
              <div>
                <label className={labelClass}>Clicks</label>
                <input className={readOnlyInputClass} type="text" value={campaign.clicks.toLocaleString()} readOnly disabled />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Click-Through Rate (CTR)</label>
                <input className={readOnlyInputClass} type="text" value={campaign.ctr > 0 ? `${campaign.ctr.toFixed(2)}%` : '—'} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Conversions</label>
                <input className={readOnlyInputClass} type="text" value={campaign.conversions.toLocaleString()} readOnly disabled />
              </div>
            </div>
          </section>

          {/* Campaign Budget */}
          <section className="space-y-4">
            <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-yellow">Campaign Budget</h3>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className={labelClass}>Total Budget</label>
                <input className={readOnlyInputClass} type="text" value={`$${campaign.budget.toLocaleString()}`} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Spent</label>
                <input className={readOnlyInputClass} type="text" value={`$${campaign.spent.toLocaleString()}`} readOnly disabled />
              </div>
              <div>
                <label className={labelClass}>Remaining</label>
                <input className={readOnlyInputClass} type="text" value={`$${(campaign.budget - campaign.spent).toLocaleString()}`} readOnly disabled />
              </div>
            </div>
          </section>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-2 px-6 py-4 border-t border-surface-light-border dark:border-surface-dark-border">
          <Button variant="secondary" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </div>
  )
}
