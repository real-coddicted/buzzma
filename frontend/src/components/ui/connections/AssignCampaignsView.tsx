import { useState, useEffect, useCallback } from 'react'
import { useSSE } from '../../../hooks/useSSE'
import { Card } from '../Card'
import { Loading } from '../Loading'
import { RupeeInput } from '../RupeeInput'
import { ConfirmModal } from '../ConfirmModal'
import { Toast } from '../Toast'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import { fetchAssignableCampaigns, type AssignableCampaign } from '../../../api/campaignApi'
import { assignToMediator } from '../../../api/assignmentApi'
import { rupeesToPaise, paiseToRupees } from '../../../utils/currency'
import { ProductThumbnail } from '../campaign/ProductThumbnail'
import { PLATFORM_COLORS, TYPE_COLORS } from '../campaign/filters/chipColors'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../../../constants/campaigns'
import type { Platform, CampaignType } from '../../../types'
import type { Connection } from '../../../types/ConnectionTypes'

interface Props {
  connection: Connection
  onBack: () => void
}

interface RowState {
  selected: boolean
  commission: string
}

function fmtDate(iso: string | null): string {
  if (!iso) return 'TBD'
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
  const parts = iso.split('-')
  if (parts.length !== 3) return iso
  const [y, m, day] = parts
  return `${months[parseInt(m, 10) - 1]} ${parseInt(day, 10)}, ${y}`
}

function formatRupees(paise: number): string {
  return `₹${paiseToRupees(paise).toLocaleString('en-IN')}`
}

function PlatformBadge({ platform }: { platform: string }) {
  const colors = PLATFORM_COLORS[platform as Platform]
  if (!colors) return <span className="text-ink-light-muted dark:text-ink-dark-muted">{platform}</span>
  return (
    <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', colors.base].join(' ')}>
      {PLATFORM_LABELS[platform as Platform]}
    </span>
  )
}

function DealTypeBadge({ campaignType }: { campaignType: string | null }) {
  if (!campaignType) return <span className="text-ink-light-muted dark:text-ink-dark-muted">—</span>
  const colors = TYPE_COLORS[campaignType as CampaignType]
  if (!colors) return <span className="text-ink-light-muted dark:text-ink-dark-muted">{campaignType}</span>
  return (
    <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', colors.base].join(' ')}>
      {CAMPAIGN_TYPE_LABELS[campaignType as CampaignType]}
    </span>
  )
}

function SlotsBar({ available, total }: { available: number; total: number }) {
  const claimed = total - available
  const pct = total > 0 ? Math.min(100, Math.round((claimed / total) * 100)) : 0
  const color = pct >= 90 ? 'bg-neon-red' : pct >= 70 ? 'bg-neon-orange' : 'bg-neon-blue'
  return (
    <div className="space-y-1">
      <span className="font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
        {claimed}/{total}
      </span>
      <div className="flex items-center gap-2">
        <div className="w-20 h-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
          <div className={['h-full rounded-full', color].join(' ')} style={{ width: `${pct}%` }} />
        </div>
        <span className={['text-[10px] font-semibold tabular-nums', pct >= 90 ? 'text-neon-red' : pct >= 70 ? 'text-neon-orange' : 'text-ink-light-muted dark:text-ink-dark-muted'].join(' ')}>
          {pct}%
        </span>
      </div>
    </div>
  )
}

export function AssignCampaignsView({ connection, onBack }: Props) {
  const [campaigns, setCampaigns] = useState<AssignableCampaign[]>([])
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState<string | null>(null)
  const [rows, setRows]           = useState<Record<string, RowState>>({})
  const [showConfirm, setShowConfirm] = useState(false)
  const [submitting, setSubmitting]   = useState(false)
  const [toast, setToast]             = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(`Assign Campaigns to ${connection.name}`, onBack)
    return clearDetail
  }, [connection.name, onBack, setDetail, clearDetail])

  const loadCampaigns = useCallback(() => {
    setLoading(true)
    fetchAssignableCampaigns(connection.toUserId)
      .then(data => {
        setCampaigns(data)
        const initial: Record<string, RowState> = {}
        data.forEach(c => { initial[c.campaignId] = { selected: false, commission: '' } })
        setRows(initial)
      })
      .catch(err => setError((err as Error).message))
      .finally(() => setLoading(false))
  }, [connection.toUserId])

  useEffect(() => { loadCampaigns() }, [loadCampaigns])

  useSSE('EVENT_TYPE_REFRESH', loadCampaigns, 'connections')

  const handleCheckbox = useCallback((campaignId: string, checked: boolean) => {
    setRows(prev => ({ ...prev, [campaignId]: { ...prev[campaignId], selected: checked } }))
  }, [])

  const handleCommission = useCallback((campaignId: string, value: string) => {
    setRows(prev => {
      const hasValue = value.trim() !== ''
      return {
        ...prev,
        [campaignId]: {
          selected: hasValue,
          commission: value,
        },
      }
    })
  }, [])

  const selectedCampaigns = campaigns.filter(c => rows[c.campaignId]?.selected)
  const hasSelection = selectedCampaigns.length > 0

  function handleSubmitClick() {
    const invalid = selectedCampaigns.filter(c => {
      const val = rows[c.campaignId]?.commission ?? ''
      return val.trim() === '' || isNaN(parseFloat(val)) || parseFloat(val) < 0
    })
    if (invalid.length > 0) {
      setToast({ message: 'Please enter a commission value for all selected campaigns.', type: 'error' })
      return
    }
    setShowConfirm(true)
  }

  async function handleConfirm() {
    setSubmitting(true)
    try {
      await assignToMediator(
        connection.toUserId,
        selectedCampaigns.map(c => ({
          campaignId: c.campaignId,
          campaignSlotId: c.slotId,
          commissionOfferedPaise: rupeesToPaise(parseFloat(rows[c.campaignId].commission)),
          adjustedCampaignPricePaise: c.campaignPricePaise,
          totalSlots: c.slotsAvailable,
        })),
      )
      setShowConfirm(false)
      setToast({ message: 'Campaigns assigned successfully.', type: 'success' })
      setRows(prev => {
        const next = { ...prev }
        selectedCampaigns.forEach(c => { next[c.campaignId] = { selected: false, commission: '' } })
        return next
      })
    } catch (err: unknown) {
      setToast({ message: (err instanceof Error ? err.message : 'Failed to assign campaigns.'), type: 'error' })
      setShowConfirm(false)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      {toast && <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />}
      {showConfirm && (
        <ConfirmModal
          title="Assign Campaigns"
          message={`Are you sure you want to proceed with assigning ${selectedCampaigns.length} campaign${selectedCampaigns.length > 1 ? 's' : ''} to ${connection.name}?`}
          confirmLabel="Assign"
          tone="blue"
          busy={submitting}
          onConfirm={handleConfirm}
          onCancel={() => setShowConfirm(false)}
        />
      )}

      <div className="max-w-7xl mx-auto flex flex-col gap-5">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Assign Campaigns to {connection.name}
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
              No assignable campaigns available.
            </div>
          ) : (
            <>
              <div className="overflow-x-auto overflow-y-auto max-h-[calc(100vh-280px)]">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover sticky top-0 z-10">
                      <th className="px-5 py-3 w-10" />
                      <th className="px-5 py-3 w-14" />
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Campaign</th>
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Brand</th>
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Platform</th>
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Type</th>
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">Start / End</th>
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Slots</th>
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">Campaign Price</th>
                      <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">Commission Offered</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
                    {campaigns.map(c => {
                      const row = rows[c.campaignId] ?? { selected: false, commission: '' }
                      return (
                        <tr
                          key={c.campaignId}
                          className={[
                            'hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors',
                            row.selected ? 'bg-neon-blue/5' : '',
                          ].join(' ')}
                        >
                          <td className="px-5 py-2.5">
                            <input
                              type="checkbox"
                              checked={row.selected}
                              onChange={e => handleCheckbox(c.campaignId, e.target.checked)}
                              className="accent-neon-blue cursor-pointer"
                            />
                          </td>
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
                            <SlotsBar available={c.slotsAvailable} total={c.totalSlots} />
                          </td>
                          <td className="px-5 py-2.5 text-ink-light-secondary dark:text-ink-dark-secondary whitespace-nowrap">
                            {formatRupees(c.campaignPricePaise)}
                          </td>
                          <td className="px-5 py-2.5">
                            <RupeeInput
                              value={row.commission}
                              onChange={v => handleCommission(c.campaignId, v)}
                              symbolOffset="left-2"
                              inputPadding="pl-5"
                              className="w-24 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg pr-2 py-1 text-ink-light-primary dark:text-ink-dark-primary outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all"
                            />
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>

              <div className="border-t border-surface-light-border dark:border-surface-dark-border px-5 py-4 flex items-center justify-between gap-4">
                <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
                  {hasSelection ? `${selectedCampaigns.length} campaign${selectedCampaigns.length > 1 ? 's' : ''} selected` : 'No campaigns selected'}
                </span>
                <button
                  disabled={!hasSelection}
                  onClick={handleSubmitClick}
                  className="px-5 py-2 rounded-lg text-sm font-semibold bg-neon-blue/15 text-neon-blue border border-neon-blue/40 hover:bg-neon-blue/25 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  Assign Campaigns
                </button>
              </div>
            </>
          )}
        </Card>
      </div>
    </>
  )
}
