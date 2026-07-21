import { useState, useMemo, useEffect } from 'react'
import { Card } from '../Card'
import { ClaimReviewToolbar } from './ClaimReviewToolbar'
import { ClaimReviewFilterDrawer } from './ClaimReviewFilterDrawer'
import { ClaimReviewActions } from './ClaimReviewActions'
import { CLAIM_REVIEW_COLUMNS, REVIEW_STATUS_CONFIG } from './claimReviewConstants'
import { ReviewStatusCell } from './ReviewStatusCell'
import { ClaimStatusBadge } from './ClaimStatusBadge'
import { Loading } from '../Loading'
import { Toast } from '../Toast'
import { getCurrentUser } from '../../../api/client'
import { fetchCampaignNames, fetchBrandNames } from '../../../api/campaignApi'
import { fetchPublishedCampaignNames, fetchPublishedBrandNames } from '../../../api/dealApi'
import { fetchConnections } from '../../../api/connectionApi'
import type { ClaimReviewItem } from '../../../types'
import { PLATFORM_LABELS } from '../../../constants/campaigns'
import { PLATFORM_COLORS } from '../campaign/filters/chipColors'
import { FilterChips, type FilterChip } from '../campaign/filters/FilterChips'
import { type ClaimReviewFilters, emptyFilters, countActiveFilters, matchesFilters } from './filters/ClaimReviewFilterTypes'
import type { TypeaheadOption } from './filters/TypeaheadMultiSelect'

// --- main grid ---

interface ClaimReviewGridProps {
  claims: ClaimReviewItem[]
  loading?: boolean
  appliedFilters: ClaimReviewFilters
  onApplyFilters: (filters: ClaimReviewFilters) => void
  onViewDetails: (claim: ClaimReviewItem) => void
  onApprove: (claim: ClaimReviewItem) => void
}

export function ClaimReviewGrid({ claims, loading = false, appliedFilters, onApplyFilters, onViewDetails, onApprove }: ClaimReviewGridProps) {
  const [search, setSearch] = useState('')
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [campaignOptions, setCampaignOptions] = useState<TypeaheadOption[]>([])
  const [brandOptions, setBrandOptions] = useState<TypeaheadOption[]>([])
  const [mediatorOptions, setMediatorOptions] = useState<TypeaheadOption[]>([])
  const [optionsError, setOptionsError] = useState<string | null>(null)
  const isMediator = getCurrentUser()?.role === 'ROLE_MEDIATOR'
  const columns = isMediator
    ? CLAIM_REVIEW_COLUMNS.map(col => col === 'Mediator Name' ? 'Buyer Name' : col)
    : CLAIM_REVIEW_COLUMNS

  useEffect(() => {
    if (!drawerOpen) return
    (isMediator ? fetchPublishedCampaignNames() : fetchCampaignNames())
      .then(list => setCampaignOptions(list.map(c => ({ value: c.id, label: c.code ? `${c.title} (${c.code})` : c.title }))))
      .catch(err => {
        console.error('Failed to load campaign filter options', err)
        setOptionsError('Failed to load campaign options for the filter.')
      })
  }, [drawerOpen, isMediator])

  useEffect(() => {
    if (!drawerOpen) return
    (isMediator ? fetchPublishedBrandNames() : fetchBrandNames())
      .then(list => setBrandOptions(list.map(b => ({ value: b, label: b }))))
      .catch(err => {
        console.error('Failed to load brand filter options', err)
        setOptionsError('Failed to load brand options for the filter.')
      })
  }, [drawerOpen, isMediator])

  useEffect(() => {
    if (!drawerOpen || isMediator) return
    fetchConnections('connected')
      .then(cs => setMediatorOptions(
        cs.filter(c => c.role === 'ROLE_MEDIATOR').map(c => ({ value: c.toUserId, label: c.code ? `${c.name} (${c.code})` : c.name }))
      ))
      .catch(err => {
        console.error('Failed to load mediator filter options', err)
        setOptionsError('Failed to load mediator options for the filter.')
      })
  }, [drawerOpen, isMediator])

  const campaignNameById = useMemo(() => new Map(campaignOptions.map(o => [o.value, o.label])), [campaignOptions])
  const mediatorNameById = useMemo(() => new Map(mediatorOptions.map(o => [o.value, o.label])), [mediatorOptions])

  const activeFilterCount = countActiveFilters(appliedFilters)

  const chips = useMemo<FilterChip[]>(() => {
    const result: FilterChip[] = []
    const f = appliedFilters

    for (const id of f.campaignIds) {
      result.push({
        key: `campaign-${id}`,
        label: campaignNameById.get(id) ?? id,
        onRemove: () => {
          const next = new Set(f.campaignIds)
          next.delete(id)
          onApplyFilters({ ...f, campaignIds: next })
        },
      })
    }

    for (const b of f.brands) {
      result.push({
        key: `brand-${b}`,
        label: b,
        onRemove: () => {
          const next = new Set(f.brands)
          next.delete(b)
          onApplyFilters({ ...f, brands: next })
        },
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

    for (const s of f.reviewStatuses) {
      result.push({
        key: `review-${s}`,
        label: REVIEW_STATUS_CONFIG[s].label,
        colorClass: REVIEW_STATUS_CONFIG[s].classes,
        onRemove: () => {
          const next = new Set(f.reviewStatuses)
          next.delete(s)
          onApplyFilters({ ...f, reviewStatuses: next })
        },
      })
    }

    for (const id of f.mediatorIds) {
      result.push({
        key: `mediator-${id}`,
        label: mediatorNameById.get(id) ?? id,
        onRemove: () => {
          const next = new Set(f.mediatorIds)
          next.delete(id)
          onApplyFilters({ ...f, mediatorIds: next })
        },
      })
    }

    return result
  }, [appliedFilters, onApplyFilters, campaignNameById, mediatorNameById])

  const filtered = useMemo(() => {
    return claims.filter(row => {
      const q = search.toLowerCase()
      const matchSearch =
        row.campaignName.toLowerCase().includes(q) ||
        row.orderId.toLowerCase().includes(q) ||
        row.mediatorName.toLowerCase().includes(q)
      return matchSearch && matchesFilters(row, appliedFilters)
    })
  }, [claims, search, appliedFilters])

  function handleAction(action: string, row: ClaimReviewItem) {
    if (action === 'details') {
      onViewDetails(row)
      return
    }
    if (action === 'approve') {
      onApprove(row)
      return
    }
  }

  return (
    <>
      <Card padded={false}>
        <ClaimReviewToolbar
          search={search}
          onSearchChange={setSearch}
          activeFilterCount={activeFilterCount}
          onOpenFilters={() => setDrawerOpen(true)}
        />

        <FilterChips chips={chips} onClearAll={() => onApplyFilters(emptyFilters())} />

        <ClaimReviewFilterDrawer
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          filters={appliedFilters}
          onApply={onApplyFilters}
          campaignOptions={campaignOptions}
          brandOptions={brandOptions}
          mediatorOptions={mediatorOptions}
          showMediatorFilter={!isMediator}
        />

        {/* table */}
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
                {columns.map(col => (
                  <th
                    key={col}
                    className={[
                      'px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted',
                      col === 'Actions' || col === 'Claim Status' || col === 'Review Status' || col === 'Mediator Verified' ? 'text-center' : 'text-left',
                    ].join(' ')}
                  >
                    {col}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
              {loading ? (
                <tr>
                  <td colSpan={columns.length} className="px-5 py-10 text-center">
                    <div className="flex justify-center text-ink-light-muted dark:text-ink-dark-muted">
                      <Loading size={32} />
                    </div>
                  </td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan={columns.length} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                    No orders match your filter.
                  </td>
                </tr>
              ) : (
                filtered.map(row => (
                  <tr
                    key={row.id}
                    onClick={() => handleAction('details', row)}
                    className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group cursor-pointer"
                  >
                    <td className="px-5 py-4">
                      <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                        {row.campaignName}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <span className="font-mono text-ink-light-primary dark:text-ink-dark-primary">
                        {row.orderId}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      {row.platform
                        ? <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', PLATFORM_COLORS[row.platform].base].join(' ')}>{PLATFORM_LABELS[row.platform]}</span>
                        : '—'}
                    </td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">
                      {row.brandName || '—'}
                    </td>
                    <td className="px-5 py-4 font-mono text-ink-light-primary dark:text-ink-dark-primary whitespace-nowrap">
                      {row.orderDate || '—'}
                    </td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">
                      {isMediator ? row.buyerName : row.mediatorName}
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex justify-center">
                        <ClaimStatusBadge status={row.claimStatus} />
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex justify-center">
                        <ReviewStatusCell status={row.reviewStatus} approvalMethod={row.approvalMethod} />
                      </div>
                    </td>
                    <td className="px-5 py-4 text-center">
                      {row.mediatorVerified
                        ? <span className="text-neon-green text-base" title="Verified">✔</span>
                        : <span className="text-ink-light-muted dark:text-ink-dark-muted text-base" title="Not verified">✗</span>
                      }
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-2 min-w-[96px]">
                        <div className="flex-1 h-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
                          <div
                            className={[
                              'h-full rounded-full transition-all',
                              row.matchPct >= 80 ? 'bg-neon-green' :
                              row.matchPct >= 50 ? 'bg-neon-yellow' :
                              'bg-neon-red',
                            ].join(' ')}
                            style={{ width: `${row.matchPct}%` }}
                          />
                        </div>
                        <span className={[
                          'text-[11px] font-semibold tabular-nums w-7 text-right',
                          row.matchPct >= 80 ? 'text-neon-green' :
                          row.matchPct >= 50 ? 'text-neon-yellow' :
                          'text-neon-red',
                        ].join(' ')}>
                          {row.matchPct}%
                        </span>
                      </div>
                    </td>
                    <td className="px-5 py-4" onClick={e => e.stopPropagation()}>
                      <ClaimReviewActions row={row} onAction={handleAction} />
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* footer */}
        <div className="px-5 py-3 border-t border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
            Showing {filtered.length} of {claims.length} orders
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
      {optionsError && <Toast message={optionsError} type="error" onDismiss={() => setOptionsError(null)} />}
    </>
  )
}
