import { useState, useMemo } from 'react'
import { Card } from '../Card'
import { StatusBadge } from '../Badge'
import { IconSearch, IconFilter } from '../icons'
import { CampaignRowActions } from './CampaignRowActions'
import type { Campaign, CampaignStatus, Platform } from '../../../types'
import { PLATFORM_LABELS } from '../../../constants/campaigns'
import { ProductThumbnail } from './ProductThumbnail'
import { Loading } from '../Loading'

const platformColors: Record<Platform, string> = {
  PLATFORM_AMAZON:   'text-neon-orange border-neon-orange/25 bg-neon-orange/10',
  PLATFORM_FLIPKART: 'text-neon-blue border-neon-blue/25 bg-neon-blue/10',
  PLATFORM_NYKAA:    'text-neon-pink border-neon-pink/25 bg-neon-pink/10',
  PLATFORM_MYNTRA:   'text-neon-purple border-neon-purple/25 bg-neon-purple/10',
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

type SortKey = keyof Pick<Campaign, 'title' | 'budget' | 'totalSlots'>

const cols: { key: SortKey; label: string }[] = [
  { key: 'title',      label: 'Campaign' },
  { key: 'budget',     label: 'Budget' },
  { key: 'totalSlots', label: 'Slots' },
]

function PlatformBadge({ platform }: { platform: Platform }) {
  return (
    <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', platformColors[platform]].join(' ')}>
      {PLATFORM_LABELS[platform]}
    </span>
  )
}

function SlotsBar({ claimed, total }: { claimed: number; total: number }) {
  const pct = total > 0 ? Math.min(100, Math.round((claimed / total) * 100)) : 0
  const color = pct >= 90 ? 'bg-neon-red' : pct >= 70 ? 'bg-neon-orange' : 'bg-neon-blue'
  return (
    <div className="flex items-center gap-2">
      <div className="w-20 h-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
        <div className={['h-full rounded-full', color].join(' ')} style={{ width: `${pct}%` }} />
      </div>
      <span className={['text-[10px] font-semibold tabular-nums', pct >= 90 ? 'text-neon-red' : pct >= 70 ? 'text-neon-orange' : 'text-ink-light-muted dark:text-ink-dark-muted'].join(' ')}>
        {pct}%
      </span>
    </div>
  )
}

interface Props {
  campaigns: Campaign[]
  loading?: boolean
  onEdit: (id: string) => void
  onCopy: (id: string) => void
  onView: (id: string) => void
}

export function CampaignTable({ campaigns, loading = false, onEdit, onCopy, onView }: Props) {
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<CampaignStatus | 'all'>('all')
  const [sortBy, setSortBy] = useState<SortKey>('totalSlots')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc')

  const filtered = useMemo(() => {
    return campaigns
      .filter(c => {
        const matchStatus = statusFilter === 'all' || c.status === statusFilter
        const matchSearch = c.title.toLowerCase().includes(search.toLowerCase())
        return matchStatus && matchSearch
      })
      .sort((a, b) => {
        const av = a[sortBy] ?? 0
        const bv = b[sortBy] ?? 0
        if (typeof av === 'string' && typeof bv === 'string') {
          return sortDir === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av)
        }
        return sortDir === 'asc' ? (av as number) - (bv as number) : (bv as number) - (av as number)
      })
  }, [campaigns, search, statusFilter, sortBy, sortDir])

  function handleSort(key: SortKey) {
    if (sortBy === key) {
      setSortDir(d => d === 'asc' ? 'desc' : 'asc')
    } else {
      setSortBy(key)
      setSortDir('desc')
    }
  }

  function SortIndicator({ col }: { col: SortKey }) {
    if (sortBy !== col) return <span className="text-ink-light-muted dark:text-ink-dark-muted opacity-40">↕</span>
    return <span className="text-neon-blue">{sortDir === 'asc' ? '↑' : '↓'}</span>
  }

  return (
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
              <th className="px-5 py-3 w-14" />
              {cols.map(col => (
                <th
                  key={col.key}
                  onClick={() => handleSort(col.key)}
                  className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted select-none cursor-pointer hover:text-ink-light-primary dark:hover:text-ink-dark-primary"
                >
                  <span className="inline-flex items-center gap-1">
                    {col.label}
                    <SortIndicator col={col.key} />
                  </span>
                </th>
              ))}
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Start/End Date</th>
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Status</th>
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Platform</th>
              <th className="px-5 py-3 text-right font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {loading ? (
              <tr>
                <td colSpan={8} className="px-5 py-12 text-center">
                  <div className="flex justify-center">
                    <Loading size={28} />
                  </div>
                </td>
              </tr>
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={8} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                  No campaigns match your filter.
                </td>
              </tr>
            ) : (
              filtered.map(c => (
                <tr key={c.id} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group">
                  <td className="px-5 py-2.5 w-14">
                    <ProductThumbnail src={c.productImageUrl} alt={c.productName} />
                  </td>
                  <td className="px-5 py-2.5">
                    <div>
                      <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">{c.title}</span>
                      <div className="text-[10px] text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 font-mono">{c.code}</div>
                    </div>
                  </td>
                  <td className="px-5 py-2.5 font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
                    ₹{c.budget.toLocaleString()}
                  </td>
                  <td className="px-5 py-2.5">
                    <div className="space-y-1">
                      <span className="font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
                        {c.slotsClaimed}/{c.totalSlots ?? '—'}
                      </span>
                      <SlotsBar claimed={c.slotsClaimed} total={c.totalSlots ?? 0} />
                    </div>
                  </td>
                  <td className="px-5 py-2.5 font-mono text-ink-light-secondary dark:text-ink-dark-secondary whitespace-nowrap">
                    {c.startDate || 'TBD'} – {c.endDate || 'TBD'}
                  </td>
                  <td className="px-5 py-2.5"><StatusBadge status={c.status} /></td>
                  <td className="px-5 py-2.5"><PlatformBadge platform={c.platform} /></td>
                  <td className="px-5 py-2.5">
                    <CampaignRowActions campaign={c} onEdit={() => onEdit(c.id)} onCopy={() => onCopy(c.id)} onView={() => onView(c.id)} />
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
  )
}
