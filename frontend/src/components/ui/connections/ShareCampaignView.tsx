import { useState, useEffect, useCallback } from 'react'
import { Card } from '../Card'
import { Loading } from '../Loading'
import { ConfirmModal } from '../ConfirmModal'
import { Toast } from '../Toast'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import { fetchShareableCampaigns, shareCampaignWithBrand, type ShareableCampaign } from '../../../api/campaignApi'
import { paiseToRupees, formatRupees as formatRupeeAmount } from '../../../utils/currency'
import { formatShortDate } from '../../../utils/time'
import { ProductThumbnail } from '../campaign/ProductThumbnail'
import { PlatformBadge, DealTypeBadge, SlotsBar } from '../campaign/CampaignBadges'
import type { Connection } from '../../../types/ConnectionTypes'

interface Props {
  connection: Connection
  onBack: () => void
}

const fmtDate = formatShortDate

function formatRupees(paise: number): string {
  return `₹${formatRupeeAmount(paiseToRupees(paise))}`
}

export function ShareCampaignView({ connection, onBack }: Props) {
  const [campaigns, setCampaigns] = useState<ShareableCampaign[]>([])
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState<string | null>(null)
  const [pendingCampaign, setPendingCampaign] = useState<ShareableCampaign | null>(null)
  const [sharing, setSharing]     = useState(false)
  const [sharedIds, setSharedIds] = useState<Set<string>>(new Set())
  const [toast, setToast]         = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(`Share Campaigns with ${connection.name}`, onBack)
    return clearDetail
  }, [connection.name, onBack, setDetail, clearDetail])

  const loadCampaigns = useCallback(() => {
    setLoading(true)
    fetchShareableCampaigns()
      .then(setCampaigns)
      .catch(err => setError((err as Error).message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadCampaigns() }, [loadCampaigns])

  async function handleConfirmShare() {
    if (!pendingCampaign) return
    setSharing(true)
    try {
      await shareCampaignWithBrand(pendingCampaign.campaignId, connection.toUserId)
      setSharedIds(prev => new Set(prev).add(pendingCampaign.campaignId))
      setToast({ message: `Shared "${pendingCampaign.campaignTitle}" with ${connection.name}.`, type: 'success' })
    } catch (err) {
      setToast({ message: (err as Error).message, type: 'error' })
    } finally {
      setSharing(false)
      setPendingCampaign(null)
    }
  }

  return (
    <>
      {toast && <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />}
      {pendingCampaign && (
        <ConfirmModal
          title="Share Campaign"
          message={`Share "${pendingCampaign.campaignTitle}" with ${connection.name}? Once shared, this cannot be undone.`}
          confirmLabel="Share"
          tone="blue"
          busy={sharing}
          onConfirm={handleConfirmShare}
          onCancel={() => setPendingCampaign(null)}
        />
      )}

      <div className="max-w-7xl mx-auto flex flex-col gap-5">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Share Campaigns with {connection.name}
          </h1>
        </div>

        <Card padded={false} className="flex flex-col">
          {loading ? (
            <div className="flex flex-col items-center gap-3 py-16 text-ink-light-muted dark:text-ink-dark-muted">
              <Loading size={32} />
              <span className="text-xs">Loading campaigns…</span>
            </div>
          ) : error ? (
            <div className="px-5 py-10 text-center text-xs text-neon-red">{error}</div>
          ) : campaigns.length === 0 ? (
            <div className="px-5 py-10 text-center text-xs text-ink-light-muted dark:text-ink-dark-muted">
              You don&apos;t have any campaigns to share yet.
            </div>
          ) : (
            <div className="overflow-x-auto overflow-y-auto max-h-[calc(100vh-280px)]">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover sticky top-0 z-10">
                    <th className="px-5 py-3 w-14" />
                    <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Campaign</th>
                    <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Brand</th>
                    <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Platform</th>
                    <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Type</th>
                    <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">Start / End</th>
                    <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Slots</th>
                    <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">Campaign Price</th>
                    <th className="px-5 py-3 w-28" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
                  {campaigns.map(c => {
                    const alreadyShared = sharedIds.has(c.campaignId)
                    return (
                      <tr key={c.campaignId} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
                        <td className="px-5 py-2.5 w-14">
                          <ProductThumbnail src={c.productImageUrl} alt={c.campaignTitle} />
                        </td>
                        <td className="px-5 py-2.5">
                          <div>
                            <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">{c.campaignTitle}</span>
                            <div className="text-[10px] text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 font-mono">{c.code}</div>
                          </div>
                        </td>
                        <td className="px-5 py-2.5 text-ink-light-secondary dark:text-ink-dark-secondary">
                          {c.productBrandName || '—'}
                        </td>
                        <td className="px-5 py-2.5">
                          <PlatformBadge platform={c.platform} />
                        </td>
                        <td className="px-5 py-2.5">
                          <DealTypeBadge campaignType={c.campaignType} />
                        </td>
                        <td className="px-5 py-2.5 font-mono text-ink-light-secondary dark:text-ink-dark-secondary whitespace-nowrap">
                          <div>{fmtDate(c.startDate)}</div>
                          <div className="text-ink-light-muted dark:text-ink-dark-muted">{fmtDate(c.endDate)}</div>
                        </td>
                        <td className="px-5 py-2.5">
                          <SlotsBar claimed={c.totalSlots - c.slotsAvailable} total={c.totalSlots} />
                        </td>
                        <td className="px-5 py-2.5 text-ink-light-secondary dark:text-ink-dark-secondary whitespace-nowrap">
                          {formatRupees(c.campaignPricePaise)}
                        </td>
                        <td className="px-5 py-2.5 text-right">
                          <button
                            disabled={alreadyShared}
                            onClick={() => setPendingCampaign(c)}
                            className="px-3 py-1.5 rounded-lg text-xs font-semibold text-neon-blue border border-neon-blue/30 bg-neon-blue/5 hover:bg-neon-blue/15 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                          >
                            {alreadyShared ? 'Shared' : 'Share'}
                          </button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      </div>
    </>
  )
}