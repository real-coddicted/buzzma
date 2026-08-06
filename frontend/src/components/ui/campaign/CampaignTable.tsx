import { useState, useMemo } from 'react'
import { Card } from '../Card'
import { StatusBadge } from '../Badge'
import { IconSearch, IconFilter } from '../icons'
import { CampaignRowActions } from './CampaignRowActions'
import { CampaignFilterDrawer } from './CampaignFilterDrawer'
import { type CampaignFilters, emptyFilters, countActiveFilters } from './filters/CampaignFilterTypes'
import { FilterChips, type FilterChip } from './filters/FilterChips'
import { PLATFORM_COLORS, TYPE_COLORS, STATUS_COLORS } from './filters/chipColors'
import type { Campaign, CampaignStatus } from '../../../types'
import { CAMPAIGN_TYPE_LABELS, PLATFORM_LABELS } from '../../../constants/campaigns'
import { ProductThumbnail } from './ProductThumbnail'
import { PlatformBadge, DealTypeBadge, SlotsBar } from './CampaignBadges'
import { Loading } from '../Loading'
import { PaginationToolbar } from '../PaginationToolbar'
import { formatShortDate as fmtDate } from '../../../utils/time'


type SortKey = keyof Pick<Campaign, 'title' | 'totalSlots'>

interface Props {
  campaigns: Campaign[]
  loading?: boolean
  appliedFilters: CampaignFilters
  onApplyFilters: (f: CampaignFilters) => void
  onEdit: (id: string) => void
  onCopy: (id: string) => void
  onView: (id: string) => void
  onPause: (id: string) => void
  onResume: (id: string) => void
  onClose: (id: string) => void
  onDelete: (id: string) => void
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function CampaignTable({ campaigns, loading = false, appliedFilters, onApplyFilters, onEdit, onCopy, onView, onPause, onResume, onClose, onDelete, currentPage, totalPages, onPageChange }: Props) {
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [sortBy, setSortBy] = useState<SortKey>('totalSlots')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc')

  const filtered = useMemo(() => {
    return campaigns
      .filter(c => {
        if (!search) return true
        const q = search.toLowerCase()
        return (
          c.title.toLowerCase().includes(q) ||
          (c.productBrandName?.toLowerCase().includes(q) ?? false) ||
          (c.code?.toLowerCase().includes(q) ?? false) ||
          PLATFORM_LABELS[c.platform].toLowerCase().includes(q) ||
          c.status.toLowerCase().includes(q) ||
          (c.campaignType != null && CAMPAIGN_TYPE_LABELS[c.campaignType].toLowerCase().includes(q))
        )
      })
      .sort((a, b) => {
        const av = a[sortBy] ?? 0
        const bv = b[sortBy] ?? 0
        if (typeof av === 'string' && typeof bv === 'string') {
          return sortDir === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av)
        }
        return sortDir === 'asc' ? (av as number) - (bv as number) : (bv as number) - (av as number)
      })
  }, [campaigns, search, sortBy, sortDir])

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

  const activeFilterCount = countActiveFilters(appliedFilters)

  const chips = useMemo<FilterChip[]>(() => {
    const result: FilterChip[] = []
    const f = appliedFilters

    if (f.brand) {
      result.push({
        key: 'brand',
        label: `Brand: ${f.brand}`,
        onRemove: () => onApplyFilters({ ...f, brand: '' }),
      })
    }

    for (const p of f.platforms) {
      result.push({
        key: `platform-${p}`,
        label: PLATFORM_LABELS[p],
        colorClass: PLATFORM_COLORS[p].base,
        onRemove: () => {
          const next = new Set(f.platforms)
          next.delete(p)
          onApplyFilters({ ...f, platforms: next })
        },
      })
    }

    for (const t of f.types) {
      result.push({
        key: `type-${t}`,
        label: CAMPAIGN_TYPE_LABELS[t],
        colorClass: TYPE_COLORS[t].base,
        onRemove: () => {
          const next = new Set(f.types)
          next.delete(t)
          onApplyFilters({ ...f, types: next })
        },
      })
    }

    for (const s of f.statuses) {
      const labels: Record<CampaignStatus, string> = {
        active: 'Active', paused: 'Paused', completed: 'Completed', closed: 'Closed', draft: 'Draft',
      }
      result.push({
        key: `status-${s}`,
        label: labels[s],
        colorClass: STATUS_COLORS[s].base,
        onRemove: () => {
          const next = new Set(f.statuses)
          next.delete(s)
          onApplyFilters({ ...f, statuses: next })
        },
      })
    }

    if (f.startDate || f.endDate) {
      const label = [f.startDate && fmtDate(f.startDate), f.endDate && fmtDate(f.endDate)].filter(Boolean).join(' – ')
      result.push({
        key: 'daterange',
        label,
        onRemove: () => onApplyFilters({ ...f, startDate: '', endDate: '' }),
      })
    }

    return result
  }, [appliedFilters, onApplyFilters])

  return (
    <Card padded={false}>
      <div className="p-4 flex items-center gap-3 border-b border-surface-light-border dark:border-surface-dark-border">
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
        <button
          onClick={() => setDrawerOpen(true)}
          className={[
            'flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium border transition-colors',
            activeFilterCount > 0
              ? 'bg-neon-blue/10 border-neon-blue/30 text-neon-blue'
              : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
          ].join(' ')}
        >
          <IconFilter size={13} />
          Filters
          {activeFilterCount > 0 && (
            <span className="inline-flex items-center justify-center w-4 h-4 rounded-full bg-neon-blue text-black text-[9px] font-bold">
              {activeFilterCount}
            </span>
          )}
        </button>
      </div>

      <FilterChips chips={chips} onClearAll={() => onApplyFilters(emptyFilters())} />

      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
              <th className="px-5 py-3 w-14" />
              <th
                onClick={() => handleSort('title')}
                className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted select-none cursor-pointer hover:text-ink-light-primary dark:hover:text-ink-dark-primary"
              >
                <span className="inline-flex items-center gap-1">Campaign <SortIndicator col="title" /></span>
              </th>
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Brand</th>
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Platform</th>
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Type</th>
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Start / End</th>
              <th
                onClick={() => handleSort('totalSlots')}
                className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted select-none cursor-pointer hover:text-ink-light-primary dark:hover:text-ink-dark-primary"
              >
                <span className="inline-flex items-center gap-1">Slots <SortIndicator col="totalSlots" /></span>
              </th>
              <th className="text-left px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Status</th>
              <th className="px-5 py-3 text-center font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {loading ? (
              <tr>
                <td colSpan={9} className="px-5 py-12 text-center">
                  <div className="flex justify-center">
                    <Loading size={28} />
                  </div>
                </td>
              </tr>
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={9} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                  No campaigns match your filters.
                </td>
              </tr>
            ) : (
              filtered.map(c => (
                <tr key={c.id} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
                  <td className="px-5 py-2.5 w-14">
                    <ProductThumbnail src={c.productImageUrl} alt={c.productName} />
                  </td>
                  <td className="px-5 py-2.5">
                    <div>
                      <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">{c.title}</span>
                      <div className="text-[10px] text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 font-mono">{c.code}</div>
                    </div>
                  </td>
                  <td className="px-5 py-2.5 text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
                    {c.productBrandName || '—'}
                  </td>
                  <td className="px-5 py-2.5"><PlatformBadge platform={c.platform} /></td>
                  <td className="px-5 py-2.5"><DealTypeBadge campaignType={c.campaignType} /></td>
                  <td className="px-5 py-2.5 font-mono text-ink-light-secondary dark:text-ink-dark-secondary whitespace-nowrap">
                    <div>{fmtDate(c.startDate)}</div>
                    <div className="text-ink-light-muted dark:text-ink-dark-muted">{fmtDate(c.endDate)}</div>
                  </td>
                  <td className="px-5 py-2.5">
                    <SlotsBar claimed={c.slotsClaimed} total={c.totalSlots ?? 0} />
                  </td>
                  <td className="px-5 py-2.5"><StatusBadge status={c.status} /></td>
                  <td className="px-5 py-2.5">
                    <CampaignRowActions
                      campaign={c}
                      onEdit={() => onEdit(c.id)}
                      onCopy={() => onCopy(c.id)}
                      onView={() => onView(c.id)}
                      onPause={() => onPause(c.id)}
                      onResume={() => onResume(c.id)}
                      onClose={() => onClose(c.id)}
                      onDelete={() => onDelete(c.id)}
                    />
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="px-5 py-3 border-t border-surface-light-border dark:border-surface-dark-border">
        <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
          Showing {filtered.length} of {campaigns.length} campaigns
        </span>
      </div>

      <PaginationToolbar
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={onPageChange}
        disabled={loading}
      />

      <CampaignFilterDrawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        filters={appliedFilters}
        onApply={onApplyFilters}
      />
    </Card>
  )
}
