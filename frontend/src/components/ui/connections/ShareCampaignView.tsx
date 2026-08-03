import { useState, useEffect, useCallback } from 'react'
import { Card } from '../Card'
import { Loading } from '../Loading'
import { ConfirmModal } from '../ConfirmModal'
import { Toast } from '../Toast'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import { fetchCampaignNames, shareCampaignWithBrand, type CampaignNameOption } from '../../../api/campaignApi'
import type { Connection } from '../../../types/ConnectionTypes'

interface Props {
  connection: Connection
  onBack: () => void
}

export function ShareCampaignView({ connection, onBack }: Props) {
  const [campaigns, setCampaigns] = useState<CampaignNameOption[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [pendingCampaign, setPendingCampaign] = useState<CampaignNameOption | null>(null)
  const [sharing, setSharing] = useState(false)
  const [sharedIds, setSharedIds] = useState<Set<string>>(new Set())
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(`Share a Campaign with ${connection.name}`, onBack)
    return clearDetail
  }, [connection.name, onBack, setDetail, clearDetail])

  const loadCampaigns = useCallback(() => {
    setLoading(true)
    fetchCampaignNames()
      .then(setCampaigns)
      .catch(err => setError((err as Error).message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadCampaigns() }, [loadCampaigns])

  async function handleConfirmShare() {
    if (!pendingCampaign) return
    setSharing(true)
    try {
      await shareCampaignWithBrand(pendingCampaign.id, connection.toUserId)
      setSharedIds(prev => new Set(prev).add(pendingCampaign.id))
      setToast({ message: `Shared "${pendingCampaign.title}" with ${connection.name}.`, type: 'success' })
    } catch (err) {
      setToast({ message: (err as Error).message, type: 'error' })
    } finally {
      setSharing(false)
      setPendingCampaign(null)
    }
  }

  return (
    <div className="max-w-3xl mx-auto space-y-5">
      {toast && <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />}
      {pendingCampaign && (
        <ConfirmModal
          title="Share Campaign"
          message={`Share "${pendingCampaign.title}" with ${connection.name}? Once shared, this cannot be undone.`}
          confirmLabel="Share"
          tone="blue"
          busy={sharing}
          onConfirm={handleConfirmShare}
          onCancel={() => setPendingCampaign(null)}
        />
      )}
      <Card>
        {loading ? (
          <Loading />
        ) : error ? (
          <div className="p-5 text-sm text-neon-red">{error}</div>
        ) : campaigns.length === 0 ? (
          <div className="p-5 text-sm text-ink-light-muted dark:text-ink-dark-muted">
            You don&apos;t have any campaigns to share yet.
          </div>
        ) : (
          <ul className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {campaigns.map(c => {
              const alreadyShared = sharedIds.has(c.id)
              return (
                <li key={c.id} className="flex items-center justify-between px-5 py-3">
                  <div>
                    <div className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">{c.title}</div>
                    <div className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{c.code}</div>
                  </div>
                  <button
                    disabled={alreadyShared}
                    onClick={() => setPendingCampaign(c)}
                    className="px-3 py-1.5 rounded-lg text-xs font-semibold text-neon-blue border border-neon-blue/30 bg-neon-blue/5 hover:bg-neon-blue/15 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    {alreadyShared ? 'Shared' : 'Share'}
                  </button>
                </li>
              )
            })}
          </ul>
        )}
      </Card>
    </div>
  )
}