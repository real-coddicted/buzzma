import { useState, useEffect, useCallback } from 'react'
import { Card } from '../Card'
import { Loading } from '../Loading'
import { PaginationToolbar } from '../PaginationToolbar'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import { fetchCampaignsSharedByMe, type SharedByMeCampaign } from '../../../api/campaignApi'
import { formatShortDate } from '../../../utils/time'

interface Props {
  onBack: () => void
}

export function SharedCampaignsPage({ onBack }: Props) {
  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail('Shared Campaigns', onBack)
    return clearDetail
  }, [onBack, setDetail, clearDetail])

  const [campaigns, setCampaigns] = useState<SharedByMeCampaign[]>([])
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages]   = useState(1)

  const loadCampaigns = useCallback((page: number) => {
    setLoading(true)
    fetchCampaignsSharedByMe(page - 1)
      .then(result => {
        setCampaigns(result.items)
        setTotalPages(result.totalPages)
      })
      .catch(err => setError((err as Error).message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadCampaigns(currentPage) }, [loadCampaigns, currentPage])

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Shared Campaigns
        </h1>
      </div>

      <Card padded={false} className="flex flex-col">
        {loading ? (
          <div className="flex flex-col items-center gap-3 py-16 text-ink-light-muted dark:text-ink-dark-muted">
            <Loading size={32} />
            <span className="text-xs">Loading shared campaigns…</span>
          </div>
        ) : error ? (
          <div className="px-5 py-10 text-center text-xs text-neon-red">{error}</div>
        ) : campaigns.length === 0 ? (
          <div className="px-5 py-10 text-center text-xs text-ink-light-muted dark:text-ink-dark-muted">
            You haven&apos;t shared any campaigns yet.
          </div>
        ) : (
          <div className="overflow-x-auto overflow-y-auto max-h-[calc(100vh-280px)]">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover sticky top-0 z-10">
                  <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Campaign</th>
                  <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Shared With</th>
                  <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">Shared On</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
                {campaigns.map(c => (
                  <tr key={`${c.campaignCode}-${c.sharedWithUserId}`} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
                    <td className="px-5 py-2.5">
                      <div>
                        <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">{c.campaignName}</span>
                        <div className="text-[10px] text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 font-mono">{c.campaignCode}</div>
                      </div>
                    </td>
                    <td className="px-5 py-2.5">
                      <div>
                        <span className="text-ink-light-primary dark:text-ink-dark-primary">{c.sharedWithUserName || '—'}</span>
                        <div className="text-[10px] text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 font-mono">{c.sharedWithUserCode}</div>
                      </div>
                    </td>
                    <td className="px-5 py-2.5 font-mono text-ink-light-secondary dark:text-ink-dark-secondary whitespace-nowrap">
                      {c.sharedAt ? formatShortDate(c.sharedAt) : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <PaginationToolbar
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
          disabled={loading}
        />
      </Card>
    </div>
  )
}