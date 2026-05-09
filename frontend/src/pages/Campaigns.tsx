import { useState, useMemo } from 'react'
import { Card } from '../components/ui/Card'
import { StatusBadge, Badge } from '../components/ui/Badge'
import { IconSearch, IconFilter, IconEdit, IconPlay, IconPause, IconInfo } from '../components/ui/icons'
import { NewCampaignButton, NewCampaignModal, type CampaignRequestDto } from '../components/ui/campaign/NewCampaignModal'
import { LaunchCampaignModal } from '../components/ui/campaign/LaunchCampaignModal'
import { CampaignDetailsModal } from '../components/ui/campaign/CampaignDetailsModal'
import { campaigns, linkedEntities, availableEntities } from '../data/mockData'
import { PLATFORM_LABELS } from '../constants/campaigns'
import type { Campaign, CampaignStatus, Platform } from '../types'

const platformColors: Record<Platform, string> = {
  PLATFORM_AMAZON:  'text-neon-orange border-neon-orange/25 bg-neon-orange/10',
  PLATFORM_FLIPKART: 'text-neon-blue border-neon-blue/25 bg-neon-blue/10',
  PLATFORM_NYKAA:   'text-neon-pink border-neon-pink/25 bg-neon-pink/10',
  PLATFORM_MYNTRA:  'text-neon-purple border-neon-purple/25 bg-neon-purple/10',
}

function PlatformBadge({ platform }: { platform: Platform }) {
  return (
    <span
      className={[
        'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border',
        platformColors[platform],
      ].join(' ')}
    >
      {PLATFORM_LABELS[platform]}
    </span>
  )
}

function SpentBar({ spent, budget }: { spent: number; budget: number }) {
  const pct = budget > 0 ? Math.min(100, Math.round((spent / budget) * 100)) : 0
  const color =
    pct >= 90 ? 'bg-neon-red' :
    pct >= 70 ? 'bg-neon-orange' :
    'bg-neon-blue'

  return (
    <div className="flex items-center gap-2">
      <div className="w-20 h-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
        <div className={['h-full rounded-full', color].join(' ')} style={{ width: `${pct}%` }} />
      </div>
      <span className={[
        'text-[10px] font-semibold tabular-nums',
        pct >= 90 ? 'text-neon-red' : pct >= 70 ? 'text-neon-orange' : 'text-ink-light-muted dark:text-ink-dark-muted',
      ].join(' ')}>
        {pct}%
      </span>
    </div>
  )
}

interface SummaryCardProps {
  label: string
  value: string | number
  accent: string
}

function SummaryCard({ label, value, accent }: SummaryCardProps) {
  return (
    <div className={[
      'rounded-xl border p-4 bg-surface-light-card dark:bg-surface-dark-card',
      'border-surface-light-border dark:border-surface-dark-border',
    ].join(' ')}>
      <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{label}</p>
      <p className={['text-xl font-bold mt-1', accent].join(' ')}>{value}</p>
    </div>
  )
}

const statusFilters: { value: CampaignStatus | 'all'; label: string }[] = [
  { value: 'all',       label: 'All' },
  { value: 'active',    label: 'Active' },
  { value: 'paused',    label: 'Paused' },
  { value: 'completed', label: 'Completed' },
  { value: 'draft',     label: 'Draft' },
]

const filterActiveClasses: Record<CampaignStatus | 'all', string> = {
  all:       'bg-neon-blue/10   text-neon-blue   border-neon-blue/30',
  active:    'bg-neon-green/10  text-neon-green  border-neon-green/30',
  paused:    'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/30',
  completed: 'bg-neon-cyan/10   text-neon-cyan   border-neon-cyan/30',
  draft:     'bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-secondary dark:text-ink-dark-secondary border-surface-light-border dark:border-surface-dark-border',
}

type SortKey = keyof Pick<Campaign, 'title' | 'budget' | 'spent' | 'impressions' | 'ctr' | 'conversions'>

export function Campaigns() {
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<CampaignStatus | 'all'>('all')
  const [sortBy, setSortBy] = useState<SortKey>('conversions')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc')
  const [launchModalOpen, setLaunchModalOpen] = useState(false)
  const [detailsModalOpen, setDetailsModalOpen] = useState(false)
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [selectedCampaignId, setSelectedCampaignId] = useState<string | null>(null)

  const selectedCampaign = campaigns.find(c => c.id === selectedCampaignId)
  const selectedLinkedEntities = selectedCampaignId ? linkedEntities[selectedCampaignId] || [] : []

  function handleCreateCampaign(dto: CampaignRequestDto) {
    console.log('Create campaign:', dto)
  }

  function handleEditCampaign(dto: CampaignRequestDto) {
    console.log('Edit campaign:', selectedCampaignId, dto)
    handleCloseEditModal()
  }

  function handleOpenEditModal(campaignId: string) {
    setSelectedCampaignId(campaignId)
    setEditModalOpen(true)
  }

  function handleCloseEditModal() {
    setEditModalOpen(false)
    setSelectedCampaignId(null)
  }

  function handleOpenLaunchModal(campaignId: string) {
    const campaign = campaigns.find(c => c.id === campaignId)
    if (campaign?.status !== 'draft') {
      return
    }
    setSelectedCampaignId(campaignId)
    setLaunchModalOpen(true)
  }

  function handleCloseLaunchModal() {
    setLaunchModalOpen(false)
    setSelectedCampaignId(null)
  }

  function handleLaunchCampaign() {
    console.log('Launching campaign:', selectedCampaignId)
    // Here you would make an API call to launch the campaign
    handleCloseLaunchModal()
  }

  function handleOpenDetailsModal(campaignId: string) {
    setSelectedCampaignId(campaignId)
    setDetailsModalOpen(true)
  }

  function handleCloseDetailsModal() {
    setDetailsModalOpen(false)
    setSelectedCampaignId(null)
  }

  const filtered = useMemo(() => {
    return campaigns
      .filter(c => {
        const matchStatus = statusFilter === 'all' || c.status === statusFilter
        const matchSearch = c.title.toLowerCase().includes(search.toLowerCase())
        return matchStatus && matchSearch
      })
      .sort((a, b) => {
        const av = a[sortBy]
        const bv = b[sortBy]
        if (typeof av === 'string' && typeof bv === 'string') {
          return sortDir === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av)
        }
        const an = av as number
        const bn = bv as number
        return sortDir === 'asc' ? an - bn : bn - an
      })
  }, [search, statusFilter, sortBy, sortDir])

  function handleSort(key: SortKey) {
    if (sortBy === key) {
      setSortDir(d => (d === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortBy(key)
      setSortDir('desc')
    }
  }

  function SortIndicator({ col }: { col: SortKey }) {
    if (sortBy !== col) return <span className="text-ink-light-muted dark:text-ink-dark-muted opacity-40">↕</span>
    return <span className="text-neon-blue">{sortDir === 'asc' ? '↑' : '↓'}</span>
  }

  const totalBudget   = campaigns.reduce((s, c) => s + c.budget, 0)
  const totalSpent    = campaigns.reduce((s, c) => s + c.spent, 0)
  const totalConv     = campaigns.reduce((s, c) => s + c.conversions, 0)
  const activeCnt     = campaigns.filter(c => c.status === 'active').length

  const cols: { key: SortKey; label: string; sortable: boolean }[] = [
    { key: 'title',       label: 'Campaign',    sortable: true },
    { key: 'budget',      label: 'Budget',      sortable: true },
    { key: 'spent',       label: 'Spent',       sortable: true },
    { key: 'impressions', label: 'Impressions', sortable: true },
    { key: 'ctr',         label: 'CTR',         sortable: true },
    { key: 'conversions', label: 'Conversions', sortable: true },
  ]

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Campaigns
          </h1>
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {campaigns.length} total campaigns · {activeCnt} active
          </p>
        </div>
        <NewCampaignButton onSubmit={handleCreateCampaign} />
      </div>

      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        <SummaryCard label="Total Budget"   value={`$${(totalBudget / 1000).toFixed(0)}K`}  accent="text-neon-blue" />
        <SummaryCard label="Total Spent"    value={`$${(totalSpent / 1000).toFixed(0)}K`}   accent="text-neon-orange" />
        <SummaryCard label="Conversions"    value={totalConv.toLocaleString()}               accent="text-neon-green" />
        <SummaryCard label="Active Now"     value={activeCnt}                                accent="text-neon-purple" />
      </div>

      <Card padded={false}>
        <div className="p-4 flex flex-col sm:flex-row gap-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <div className="flex items-center gap-2 bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg px-3 py-2 flex-1 min-w-0 max-w-xs">
            <IconSearch size={14} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
            <input
              type="text"
              placeholder="Search campaigns…"
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="bg-transparent text-xs outline-none flex-1 text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
            />
          </div>
          <div className="flex items-center gap-1.5 flex-wrap">
            <IconFilter size={13} className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
            {statusFilters.map(f => (
              <button
                key={f.value}
                onClick={() => setStatusFilter(f.value)}
                className={[
                  'px-3 py-1 rounded-full text-xs font-medium border transition-all',
                  statusFilter === f.value
                    ? filterActiveClasses[f.value]
                    : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
                ].join(' ')}
              >
                {f.label}
              </button>
            ))}
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
                {cols.map(col => (
                  <th
                    key={col.key}
                    onClick={() => col.sortable && handleSort(col.key)}
                    className={[
                      'text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted select-none',
                      col.sortable ? 'cursor-pointer hover:text-ink-light-primary dark:hover:text-ink-dark-primary' : '',
                    ].join(' ')}
                  >
                    <span className="inline-flex items-center gap-1">
                      {col.label}
                      {col.sortable && <SortIndicator col={col.key} />}
                    </span>
                  </th>
                ))}
                <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                  Status
                </th>
                <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                  Platform
                </th>
                <th className="px-5 py-3 text-right font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={9} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                    No campaigns match your filter.
                  </td>
                </tr>
              ) : (
                filtered.map(c => (
                  <tr
                    key={c.id}
                    className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group"
                  >
                    <td className="px-5 py-4">
                      <div>
                        <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                          {c.title}
                        </span>
                        <div className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted mt-0.5 font-mono">
                          {c.startDate} – {c.endDate}
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4 font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
                      ${c.budget.toLocaleString()}
                    </td>
                    <td className="px-5 py-4">
                      <div className="space-y-1">
                        <span className="font-mono text-ink-light-primary dark:text-ink-dark-primary">
                          ${c.spent.toLocaleString()}
                        </span>
                        <SpentBar spent={c.spent} budget={c.budget} />
                      </div>
                    </td>
                    <td className="px-5 py-4 font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
                      {c.impressions >= 1_000_000
                        ? `${(c.impressions / 1_000_000).toFixed(1)}M`
                        : c.impressions >= 1_000
                        ? `${(c.impressions / 1_000).toFixed(0)}K`
                        : c.impressions.toString()}
                    </td>
                    <td className="px-5 py-4 font-mono font-semibold text-neon-cyan">
                      {c.ctr > 0 ? `${c.ctr.toFixed(2)}%` : '—'}
                    </td>
                    <td className="px-5 py-4 font-mono font-semibold text-neon-green">
                      {c.conversions > 0 ? c.conversions.toLocaleString() : '—'}
                    </td>
                    <td className="px-5 py-4">
                      <StatusBadge status={c.status} />
                    </td>
                    <td className="px-5 py-4">
                      <PlatformBadge platform={c.platform} />
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        {c.status === 'draft' ? (
                          <button
                            title="Edit"
                            onClick={() => handleOpenEditModal(c.id)}
                            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-blue hover:bg-neon-blue/10 transition-colors"
                          >
                            <IconEdit size={13} />
                          </button>
                        ) : (
                          <button
                            title="View Details"
                            onClick={() => handleOpenDetailsModal(c.id)}
                            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-cyan hover:bg-neon-cyan/10 transition-colors"
                          >
                            <IconInfo size={13} />
                          </button>
                        )}
                        {c.status === 'active' ? (
                          <button
                            title="Pause"
                            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-yellow hover:bg-neon-yellow/10 transition-colors"
                          >
                            <IconPause size={13} />
                          </button>
                        ) : c.status === 'paused' || c.status === 'draft' ? (
                          <button
                            title="Launch"
                            onClick={() => handleOpenLaunchModal(c.id)}
                            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-green hover:bg-neon-green/10 transition-colors"
                          >
                            <IconPlay size={13} />
                          </button>
                        ) : null}
                        <Badge variant="neutral">
                          {Math.round((c.spent / (c.budget || 1)) * 100)}%
                        </Badge>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="px-5 py-3 border-t border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
            Showing {filtered.length} of {campaigns.length} campaigns
          </span>
          <div className="flex items-center gap-1">
            <button className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40" disabled>
              Previous
            </button>
            <button className="px-3 py-1.5 text-xs rounded-lg bg-neon-blue/10 border border-neon-blue/30 text-neon-blue font-semibold">
              1
            </button>
            <button className="px-3 py-1.5 text-xs rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors disabled:opacity-40" disabled>
              Next
            </button>
          </div>
        </div>
      </Card>

      <LaunchCampaignModal
        open={launchModalOpen}
        campaignName={selectedCampaign?.title || ''}
        linkedEntities={selectedLinkedEntities}
        availableEntities={availableEntities}
        onClose={handleCloseLaunchModal}
        onLaunch={handleLaunchCampaign}
      />

      <CampaignDetailsModal
        open={detailsModalOpen}
        campaign={selectedCampaign || null}
        onClose={handleCloseDetailsModal}
      />

      <NewCampaignModal
        open={editModalOpen}
        campaign={selectedCampaign || null}
        onClose={handleCloseEditModal}
        onSubmit={handleEditCampaign}
      />
    </div>
  )
}
