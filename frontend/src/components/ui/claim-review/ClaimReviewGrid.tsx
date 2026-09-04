import { useState, useMemo, useEffect, useCallback } from 'react'
import { paiseToRupees, rupeesToPaise, formatRupees } from '../../../utils/currency'
import { RupeeInput } from '../RupeeInput'
import { Card } from '../Card'
import { ClaimReviewToolbar } from './ClaimReviewToolbar'
import { ClaimReviewFilterDrawer } from './ClaimReviewFilterDrawer'
import { ClaimReviewActions } from './ClaimReviewActions'
import { CLAIM_REVIEW_COLUMNS, CLAIM_STATUS_CONFIG } from './claimReviewConstants'
import { ClaimStatusBadge } from './ClaimStatusBadge'
import { AlertModal } from '../AlertModal'
import { ConfirmModal } from '../ConfirmModal'
import { Loading } from '../Loading'
import { Toast } from '../Toast'
import { IconCalendar } from '../icons'
import { getCurrentUser } from '../../../api/client'
import { fetchCampaignNames, fetchBrandNames, fetchSharedCampaignNames } from '../../../api/campaignApi'
import { fetchPublishedCampaignNames, fetchPublishedBrandNames } from '../../../api/dealApi'
import { fetchConnections } from '../../../api/connectionApi'
import { downloadClaimReviewReport } from '../../../api/claimApi'
import type { ClaimReviewItem } from '../../../types'
import { PLATFORM_LABELS } from '../../../constants/campaigns'
import { PLATFORM_COLORS, CLAIM_STATUS_COLORS } from '../campaign/filters/chipColors'
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
  onApprove: (claim: ClaimReviewItem, amountApprovedPaise?: number) => void
  onBulkApprove: (claims: ClaimReviewItem[], approvedAmountsPaise: Record<string, number>) => Promise<void>
  onOpenImport: () => void
  initialCampaignOption?: TypeaheadOption
}

export function ClaimReviewGrid({ claims, loading = false, appliedFilters, onApplyFilters, onViewDetails, onApprove, onBulkApprove, onOpenImport, initialCampaignOption }: ClaimReviewGridProps) {
  const [search, setSearch] = useState('')
  const [approvedAmounts, setApprovedAmounts] = useState<Record<string, string>>({})
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())
  const [campaignOptions, setCampaignOptions] = useState<TypeaheadOption[]>(initialCampaignOption ? [initialCampaignOption] : [])
  const [brandOptions, setBrandOptions] = useState<TypeaheadOption[]>([])
  const [mediatorOptions, setMediatorOptions] = useState<TypeaheadOption[]>([])
  const [optionsError, setOptionsError] = useState<string | null>(null)
  const [downloadingReport, setDownloadingReport] = useState(false)
  const [showBulkAmountError, setShowBulkAmountError] = useState(false)
  const [showBulkConfirm, setShowBulkConfirm] = useState(false)
  const [pendingBulkAmounts, setPendingBulkAmounts] = useState<Record<string, number>>({})
  const [bulkApproving, setBulkApproving] = useState(false)
  const isMediator = getCurrentUser()?.role === 'ROLE_MEDIATOR'
  const isBrand = getCurrentUser()?.role === 'ROLE_BRAND'
  const isAgency = getCurrentUser()?.role === 'ROLE_AGENCY'
  const columns = (isMediator || isBrand)
    ? CLAIM_REVIEW_COLUMNS.map(col => col === 'Mediator / Buyer Name' ? 'Buyer Name' : col)
    : CLAIM_REVIEW_COLUMNS

  useEffect(() => {
    if (!drawerOpen) return
    (isMediator ? fetchPublishedCampaignNames() : isBrand ? fetchSharedCampaignNames() : fetchCampaignNames())
      .then(list => setCampaignOptions(list.map(c => ({ value: c.id, label: c.code ? `${c.title} (${c.code})` : c.title }))))
      .catch(err => {
        console.error('Failed to load campaign filter options', err)
        setOptionsError('Failed to load campaign options for the filter.')
      })
  }, [drawerOpen, isMediator, isBrand])

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

    for (const s of f.claimStatuses) {
      result.push({
        key: `claim-status-${s}`,
        label: CLAIM_STATUS_CONFIG[s].label,
        colorClass: CLAIM_STATUS_COLORS[s].base,
        onRemove: () => {
          const next = new Set(f.claimStatuses)
          next.delete(s)
          onApplyFilters({ ...f, claimStatuses: next })
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

  async function handleDownloadReport() {
    setDownloadingReport(true)
    try {
      await downloadClaimReviewReport({
        campaignIds: appliedFilters.campaignIds,
        mediatorIds: appliedFilters.mediatorIds,
        brands: appliedFilters.brands,
        platforms: appliedFilters.platforms,
        claimStatuses: appliedFilters.claimStatuses,
      })
    } catch (err) {
      console.error('Failed to download claim review report', err)
      setOptionsError('Failed to download the report.')
    } finally {
      setDownloadingReport(false)
    }
  }

  useEffect(() => {
    setApprovedAmounts(prev => {
      const updates: Record<string, string> = {}
      for (const claim of claims) {
        if (claim.amountApprovedPaise != null && !(claim.id in prev)) {
          updates[claim.id] = paiseToRupees(claim.amountApprovedPaise).toFixed(2)
        }
      }
      return Object.keys(updates).length ? { ...prev, ...updates } : prev
    })
  }, [claims])

  const handleAmountChange = useCallback((id: string, value: string) => {
    if (value !== '' && parseFloat(value) < 0) return
    setApprovedAmounts(prev => ({ ...prev, [id]: value }))
  }, [])

  const selectedClaims = useMemo(
    () => filtered.filter(r => selectedIds.has(r.id)),
    [filtered, selectedIds]
  )
  const selectableRows = useMemo(
    () => filtered.filter(r => r.claimStatus !== 'APPROVED' && r.claimStatus !== 'REJECTED' && r.claimStatus !== 'PROOF_REJECTED'),
    [filtered]
  )
  const allSelected = selectableRows.length > 0 && selectableRows.every(r => selectedIds.has(r.id))
  const allSelectedApprovable = selectedClaims.length > 0 && selectedClaims.every(r => r.isUnderReview)

  function toggleRow(id: string) {
    setSelectedIds(prev => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  function toggleAll(checked: boolean) {
    setSelectedIds(checked ? new Set(selectableRows.map(r => r.id)) : new Set())
  }

  function handleBulkApproveClick() {
    const missing = selectedClaims.filter(c => !approvedAmounts[c.id]?.trim())
    if (missing.length > 0) {
      setShowBulkAmountError(true)
      return
    }
    const amountsPaise: Record<string, number> = {}
    for (const claim of selectedClaims) {
      amountsPaise[claim.id] = rupeesToPaise(parseFloat(approvedAmounts[claim.id]))
    }
    setPendingBulkAmounts(amountsPaise)
    setShowBulkConfirm(true)
  }

  async function handleBulkConfirm() {
    setBulkApproving(true)
    try {
      await onBulkApprove(selectedClaims, pendingBulkAmounts)
      setSelectedIds(new Set())
    } finally {
      setBulkApproving(false)
      setShowBulkConfirm(false)
    }
  }

  function handleAction(action: string, row: ClaimReviewItem) {
    if (action === 'details') {
      onViewDetails(row)
      return
    }
    if (action === 'approve') {
      const raw = approvedAmounts[row.id]?.trim()
      if (!raw) {
        setOptionsError('Approved amount is mandatory to approve a claim.')
        return
      }
      onApprove(row, rupeesToPaise(parseFloat(raw)))
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
          onDownloadReport={handleDownloadReport}
          downloadingReport={downloadingReport}
          showExport={isAgency || isBrand}
          showImport={isAgency}
          onOpenImport={onOpenImport}
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
                {!isMediator && (
                  <th className="pl-5 pr-2 py-3 w-8">
                    <input
                      type="checkbox"
                      checked={allSelected}
                      onChange={e => toggleAll(e.target.checked)}
                      className="w-4 h-4 accent-neon-blue cursor-pointer"
                    />
                  </th>
                )}
                {columns.map(col => (
                  <th
                    key={col}
                    className={[
                      'px-5 py-3 font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted',
                      col === 'Actions' || col === 'Claim Status' || col === 'Amount' ? 'text-center' : 'text-left',
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
                  <td colSpan={columns.length + (isMediator ? 0 : 1)} className="px-5 py-10 text-center">
                    <div className="flex justify-center text-ink-light-muted dark:text-ink-dark-muted">
                      <Loading size={32} />
                    </div>
                  </td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan={columns.length + (isMediator ? 0 : 1)} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                    No orders match your filter.
                  </td>
                </tr>
              ) : (
                filtered.map(row => (
                  <tr
                    key={row.id}
                    onClick={() => handleAction('details', row)}
                    className={[
                      'hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors group cursor-pointer',
                      selectedIds.has(row.id) ? 'bg-neon-blue/5' : '',
                    ].join(' ')}
                  >
                    {!isMediator && (
                      <td className="pl-5 pr-2 py-4 w-8" onClick={e => e.stopPropagation()}>
                        <input
                          type="checkbox"
                          checked={selectedIds.has(row.id)}
                          onChange={() => toggleRow(row.id)}
                          disabled={row.claimStatus === 'APPROVED' || row.claimStatus === 'REJECTED' || row.claimStatus === 'PROOF_REJECTED'}
                          className="w-4 h-4 accent-neon-blue cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed"
                        />
                      </td>
                    )}
                    <td className="px-5 py-4">
                      <div className="flex flex-col gap-1">
                        <span className="font-bold font-mono text-ink-light-primary dark:text-ink-dark-primary">
                          {row.orderId}
                        </span>
                        <div className="border-t border-surface-light-border dark:border-surface-dark-border pt-1 flex items-center gap-1 text-ink-light-muted dark:text-ink-dark-muted">
                          <IconCalendar size={11} />
                          <span className="font-semibold text-[10px] uppercase tracking-wider">ORD</span>
                          <span className="font-mono text-xs">{row.orderDate || '—'}</span>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex flex-col gap-1">
                        <span className="text-ink-light-primary dark:text-ink-dark-primary">
                          {row.campaignName}
                        </span>
                        <div className="border-t border-surface-light-border dark:border-surface-dark-border pt-1">
                          {row.platform
                            ? <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', PLATFORM_COLORS[row.platform].base].join(' ')}>{PLATFORM_LABELS[row.platform]}</span>
                            : <span className="text-ink-light-muted dark:text-ink-dark-muted">—</span>}
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4 text-ink-light-muted dark:text-ink-dark-muted">
                      {row.brandName || '—'}
                    </td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">
                      {isMediator || isBrand ? (
                        row.buyerName
                      ) : (
                        <div className="flex flex-col gap-1">
                          <span>{row.mediatorName}</span>
                          <div className="border-t border-surface-light-border dark:border-surface-dark-border pt-1">
                            {row.buyerName}
                          </div>
                        </div>
                      )}
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex justify-center">
                        <ClaimStatusBadge status={row.claimStatus} approvalMethod={row.approvalMethod} />
                      </div>
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
                      <div className="flex flex-col items-center gap-1.5">
                        <div className="flex items-center gap-1.5">
                          <span className="text-[10px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">Claimed:</span>
                          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">
                            {row.amountPaise != null ? `₹${formatRupees(paiseToRupees(row.amountPaise))}` : '—'}
                          </span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <span className="text-[10px] font-bold uppercase tracking-wider text-ink-light-primary dark:text-ink-dark-primary">Approved:</span>
                          <RupeeInput
                            value={approvedAmounts[row.id] ?? ''}
                            onChange={v => handleAmountChange(row.id, v)}
                            symbolOffset="left-1.5"
                            inputPadding="pl-4"
                            disabled={row.claimStatus === 'APPROVED' || row.claimStatus === 'REJECTED'}
                            className="w-20 pr-2 py-0.5 text-xs rounded border border-surface-light-border dark:border-surface-dark-border bg-transparent text-ink-light-primary dark:text-ink-dark-primary focus:outline-none focus:border-neon-green/50 disabled:opacity-50 disabled:cursor-not-allowed"
                          />
                        </div>
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
      {showBulkAmountError && (
        <AlertModal
          title="Missing Approved Amount"
          message="One or more selected claims are missing the approved amount value. To proceed with approval, make sure that each selected claim has a corresponding approved amount value present."
          onDismiss={() => setShowBulkAmountError(false)}
        />
      )}
      {showBulkConfirm && (
        <ConfirmModal
          title="Approve All Selected Claims"
          message="Are you sure? This will approve all selected claims and cannot be reversed."
          confirmLabel="Yes, Proceed"
          cancelLabel="Cancel"
          tone="blue"
          busy={bulkApproving}
          onConfirm={handleBulkConfirm}
          onCancel={() => setShowBulkConfirm(false)}
        />
      )}
      {!isMediator && selectedIds.size > 0 && (
        <button
          onClick={() => allSelectedApprovable && handleBulkApproveClick()}
          disabled={!allSelectedApprovable}
          title={!allSelectedApprovable ? 'One or more of the claims are not eligible for approval yet due to pending process' : undefined}
          className={[
            'fixed bottom-7 right-8 flex items-center gap-2.5 px-5 py-3 rounded-xl font-semibold text-sm shadow-lg z-50',
            allSelectedApprovable
              ? 'bg-neon-green text-surface-dark-base shadow-neon-green/40 hover:brightness-110 cursor-pointer'
              : 'bg-neon-green/40 text-surface-dark-base/70 cursor-not-allowed',
          ].join(' ')}
        >
          <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M2 8l4 4 8-8" />
          </svg>
          Approve All Claims
          <span className="bg-white/25 rounded-full px-2 py-0.5 text-xs font-bold">
            {selectedIds.size}
          </span>
        </button>
      )}
    </>
  )
}
