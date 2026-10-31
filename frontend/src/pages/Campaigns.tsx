import { useState, useEffect, useCallback } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'
import { IconPlus, IconMegaphone } from '../components/ui/icons'
import { NewCampaignPage } from '../components/ui/campaign/NewCampaignPage'
import { CampaignTable } from '../components/ui/campaign/CampaignTable'
import { CampaignSummaryCards } from '../components/ui/campaign/CampaignSummaryCards'
import { Loading } from '../components/ui/Loading'
import { SharedCampaignsPage } from '../components/ui/campaign/SharedCampaignsPage'
import type { Campaign, CampaignRequestDto, CampaignStatus, Platform, CampaignType, PromotionCategory } from '../types'
import { createCampaign, updateCampaign, fetchCampaignById, copyCampaign, pauseCampaign, resumeCampaign, closeCampaign, deleteCampaign, searchCampaigns, fetchSharedCampaigns, type CampaignResponseDto, type PagedCampaigns } from '../api/campaignApi'
import { getCurrentUser } from '../api/client'
import { yyyymmddToIso } from '../utils/time'
import { type CampaignFilters, emptyFilters } from '../components/ui/campaign/filters/CampaignFilterTypes'
import type { CampaignForm } from '../components/ui/campaign/campaignFormConstants'
import { paiseToRupees } from '../utils/currency'
import { Toast } from '../components/ui/Toast'
import { ConfirmModal } from '../components/ui/ConfirmModal'
import { ToggleSwitch } from '../components/ui/ToggleSwitch'
import { useSSE } from '../hooks/useSSE'
import { VIEW_ONLY_TOOLTIP, useViewOnly } from '../contexts/ViewOnlyContext'

type CampaignTab = 'active' | 'archive'

/** Active = still actionable; Archive = terminal. ASSIGNED campaigns surface as "Active" everywhere else, so they belong here too. */
const TAB_STATUSES: Record<CampaignTab, Set<CampaignStatus>> = {
  active: new Set<CampaignStatus>(['active', 'paused', 'draft']),
  archive: new Set<CampaignStatus>(['closed', 'completed']),
}

/** Narrows a filter set's statuses to the current tab, or falls back to the tab's full set when no status filter is applied. */
function withTabStatuses(filters: CampaignFilters, tab: CampaignTab): CampaignFilters {
  const tabStatuses = TAB_STATUSES[tab]
  const statuses = filters.statuses.size > 0
    ? new Set([...filters.statuses].filter(s => tabStatuses.has(s)))
    : tabStatuses
  return { ...filters, statuses }
}


function responseToForm(dto: CampaignResponseDto): CampaignForm {
  const assignments = dto.assignments ?? []
  return {
    title: dto.title ?? '',
    category: (dto.category ?? 'ECOMMERCE') as PromotionCategory,
    platform: (dto.platform ?? '') as Platform | '',
    productBrandName: dto.productBrandName ?? '',
    productName: dto.productName ?? '',
    productImageUrl: dto.productImageUrl ?? '',
    productUrl: dto.productLink ?? '',
    sellerName: dto.sellerName ?? '',
    originalPriceRupees: dto.productPricePaise != null ? paiseToRupees(dto.productPricePaise).toFixed(2) : '',
    campaignPriceRupees: dto.campaignPricePaise != null ? paiseToRupees(dto.campaignPricePaise).toFixed(2) : '',
    commissionToAllRupees: dto.commissionToAllPaise != null ? paiseToRupees(dto.commissionToAllPaise).toFixed(2) : '',
    returnWindowDays: dto.returnWindowDays?.toString() ?? '',
    campaignType: (dto.campaignType ?? '') as CampaignType | '',
    startDate: yyyymmddToIso(dto.startDate),
    endDate: yyyymmddToIso(dto.endDate),
    totalSlots: dto.totalSlots?.toString() ?? '',
    openToAll: dto.openToAll ?? false,
    affiliateLinkAllowed: dto.affiliateLinkAllowed ?? false,
    assignees: assignments.map(a => ({
      id: a.assigneeId ?? '',
      name: a.assigneeName ?? '',
      slotsAvailable: a.slotOffered ?? 0,
      commissionOffered: paiseToRupees(a.commissionOfferedPaise ?? 0),
    })),
    termsAndConditions: dto.termsAndConditions ?? '',
    requiredSteps: dto.requiredSteps ?? ['ORDER'],
    exchangeProducts: (dto.exchangeProducts ?? []).map(p => ({
      productName: p.productName ?? '',
      productImageUrl: p.productImageUrl ?? '',
      selected: true,
      prefilled: true,
    })),
  }
}

interface CampaignDetail {
  form: CampaignForm
  status: NonNullable<CampaignResponseDto['status']>
  code: string | null
}

export function Campaigns() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const view = searchParams.get('view')
  const campaignId = searchParams.get('id')
  const showDetail = view === 'new' || view === 'edit' || view === 'view'
  const isViewMode = view === 'view'
  const currentRole = getCurrentUser()?.role
  const viewOnly = useViewOnly()
  const isBrand = currentRole === 'ROLE_BRAND'
  const fetchCampaignsPage = isBrand ? fetchSharedCampaigns : searchCampaigns

  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [loading, setLoading] = useState(true)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [detail, setDetail] = useState<CampaignDetail | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [confirmPauseId, setConfirmPauseId] = useState<string | null>(null)
  const [confirmCloseId, setConfirmCloseId] = useState<string | null>(null)
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)
  const [appliedFilters, setAppliedFilters] = useState<CampaignFilters>(emptyFilters)
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [tab, setTab] = useState<CampaignTab>('active')
  const [hideEmptyClosed, setHideEmptyClosed] = useState(true)

  const applyPage = useCallback((filters: CampaignFilters, forTab: CampaignTab, page: number): Promise<PagedCampaigns> => {
    setLoading(true)
    return fetchCampaignsPage(withTabStatuses(filters, forTab), page - 1)
      .then(result => {
        setCampaigns(result.items)
        setTotalPages(result.totalPages)
        setCurrentPage(page)
        return result
      })
      .finally(() => setLoading(false))
  }, [fetchCampaignsPage])

  const loadCampaigns = useCallback((page = 1) => {
    applyPage(emptyFilters(), tab, page)
      .catch(err => setErrorMsg((err as Error).message || 'Failed to load campaigns.'))
  }, [applyPage, tab])

  useEffect(() => { loadCampaigns() }, [loadCampaigns])

  const handleApplyFilters = useCallback(async (filters: CampaignFilters, page = 1) => {
    setAppliedFilters(filters)
    try {
      await applyPage(filters, tab, page)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Search failed.')
    }
  }, [applyPage, tab])

  function handlePageChange(page: number) {
    handleApplyFilters(appliedFilters, page)
  }

  function handleTabChange(newTab: CampaignTab) {
    setTab(newTab)
    applyPage(appliedFilters, newTab, 1)
      .catch(err => setErrorMsg((err as Error).message || 'Failed to load campaigns.'))
  }

  useSSE('EVENT_TYPE_REFRESH', loadCampaigns, 'campaigns')

  useEffect(() => {
    if (!campaignId || (view !== 'edit' && view !== 'view')) {
      setDetail(null)
      return
    }
    let cancelled = false
    setDetailLoading(true)
    fetchCampaignById(campaignId)
      .then(dto => {
        if (cancelled) return
        setDetail({
          form: responseToForm(dto),
          status: dto.status ?? 'CAMPAIGN_STATUS_DRAFT',
          code: dto.code ?? null,
        })
      })
      .catch(err => { if (!cancelled) setErrorMsg((err as Error).message || 'Failed to load campaign.') })
      .finally(() => { if (!cancelled) setDetailLoading(false) })
    return () => { cancelled = true }
  }, [campaignId, view])

  async function handleCopyCampaign(id: string) {
    try {
      await copyCampaign(id)
      handleApplyFilters(appliedFilters, currentPage)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to copy campaign.')
    }
  }

  function handleViewCampaign(id: string) {
    setSearchParams({ view: 'view', id })
  }

  function handleViewClaims(id: string, title: string) {
    navigate('/claim-review?campaignId=' + encodeURIComponent(id) + '&campaignName=' + encodeURIComponent(title))
  }

  function handleRequestClose(id: string) {
    setConfirmCloseId(id)
  }

  async function handleConfirmClose() {
    if (!confirmCloseId) return
    setActioningId(confirmCloseId)
    try {
      await closeCampaign(confirmCloseId)
      handleApplyFilters(appliedFilters, currentPage)
      setConfirmCloseId(null)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to close campaign.')
    } finally {
      setActioningId(null)
    }
  }

  function handleRequestDelete(id: string) {
    setConfirmDeleteId(id)
  }

  async function handleConfirmDelete() {
    if (!confirmDeleteId) return
    setActioningId(confirmDeleteId)
    try {
      await deleteCampaign(confirmDeleteId)
      handleApplyFilters(appliedFilters, currentPage)
      setConfirmDeleteId(null)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to delete campaign.')
    } finally {
      setActioningId(null)
    }
  }

  function handleEditCampaign(id: string) {
    setSearchParams({ view: 'edit', id })
  }

  function handleRequestPause(id: string) {
    setConfirmPauseId(id)
  }

  async function handleConfirmPause() {
    if (!confirmPauseId) return
    setActioningId(confirmPauseId)
    try {
      await pauseCampaign(confirmPauseId)
      handleApplyFilters(appliedFilters, currentPage)
      setConfirmPauseId(null)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to pause campaign.')
    } finally {
      setActioningId(null)
    }
  }

  async function handleResumeCampaign(id: string) {
    try {
      await resumeCampaign(id)
      handleApplyFilters(appliedFilters, currentPage)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to resume campaign.')
    }
  }

  function handleBack() {
    setSearchParams({})
  }

  async function handleCreateCampaign(dto: CampaignRequestDto): Promise<void> {
    await createCampaign(dto)
    handleBack()
    handleApplyFilters(appliedFilters, currentPage)
  }

  async function handleUpdateCampaign(dto: CampaignRequestDto): Promise<void> {
    if (!campaignId || !detail?.status) return
    await updateCampaign(campaignId, dto, detail.status)
    handleBack()
    handleApplyFilters(appliedFilters, currentPage)
  }

  const totalSpent  = campaigns.reduce((s, c) => s + c.spent, 0)
  const totalConv   = campaigns.reduce((s, c) => s + c.conversions, 0)
  const activeCnt   = campaigns.filter(c => c.status === 'active').length
  const visibleCampaigns = tab === 'archive' && hideEmptyClosed
    ? campaigns.filter(c => c.slotsClaimed > 0)
    : campaigns
  const hiddenEmptyCount = campaigns.length - visibleCampaigns.length

  if (showDetail) {
    if (detailLoading) {
      return (
        <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted">
          <Loading size={32} />
        </div>
      )
    }
    return (
      <NewCampaignPage
        onBack={handleBack}
        onSubmit={campaignId ? handleUpdateCampaign : handleCreateCampaign}
        initialForm={detail?.form}
        readOnly={isViewMode || viewOnly}
        campaignCode={detail?.code ?? undefined}
      />
    )
  }

  if (view === 'shared') {
    return <SharedCampaignsPage onBack={handleBack} />
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Campaigns
          </h1>
        </div>
        <div className="flex items-center gap-2">
          {currentRole === 'ROLE_AGENCY' && (
            <Button variant="yellowSoft" size="md" leftIcon={<IconMegaphone size={14} />} onClick={() => setSearchParams({ view: 'shared' })}>
              Shared Campaigns
            </Button>
          )}
          {!isBrand && (
            <Button variant="primary" size="md" leftIcon={<IconPlus size={14} />} onClick={() => setSearchParams({ view: 'new' })} disabled={viewOnly} title={viewOnly ? VIEW_ONLY_TOOLTIP : undefined}>
              New Campaign
            </Button>
          )}
        </div>
      </div>

      {!isBrand && (
        <CampaignSummaryCards
          totalSpent={totalSpent}
          totalConversions={totalConv}
          activeCount={activeCnt}
          total={campaigns.length}
        />
      )}

      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="inline-flex items-center gap-1 p-1 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border">
          {(['active', 'archive'] as CampaignTab[]).map(t => (
            <button
              key={t}
              onClick={() => handleTabChange(t)}
              className={[
                'px-3 py-1.5 rounded-md text-xs font-semibold transition-colors',
                tab === t
                  ? 'bg-surface-light-card dark:bg-surface-dark-card text-ink-light-primary dark:text-ink-dark-primary shadow-sm'
                  : 'text-ink-light-secondary dark:text-ink-dark-secondary hover:text-ink-light-primary dark:hover:text-ink-dark-primary',
              ].join(' ')}
            >
              {t === 'active' ? 'Active' : 'Archive'}
            </button>
          ))}
        </div>

        {tab === 'archive' && (
          <ToggleSwitch
            checked={hideEmptyClosed}
            onChange={setHideEmptyClosed}
            label="Hide campaigns no mediator ever claimed a slot on"
            hint={hiddenEmptyCount > 0 ? `${hiddenEmptyCount} hidden` : undefined}
          />
        )}
      </div>

      <CampaignTable
        campaigns={visibleCampaigns}
        loading={loading}
        appliedFilters={appliedFilters}
        onApplyFilters={handleApplyFilters}
        onEdit={handleEditCampaign}
        onCopy={handleCopyCampaign}
        onView={handleViewCampaign}
        onPause={handleRequestPause}
        onResume={handleResumeCampaign}
        onClose={handleRequestClose}
        onDelete={handleRequestDelete}
        onViewClaims={handleViewClaims}
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
        readOnly={isBrand}
      />

      {confirmPauseId && (
        <ConfirmModal
          title="Pause campaign?"
          message="Are you sure you want to pause this campaign? Claims will stop being accepted until it is resumed."
          confirmLabel="Pause Campaign"
          tone="red"
          busy={actioningId === confirmPauseId}
          onConfirm={handleConfirmPause}
          onCancel={() => setConfirmPauseId(null)}
        />
      )}

      {confirmCloseId && (
        <ConfirmModal
          title="Close campaign?"
          message="Are you sure you want to close this campaign? This can't be undone."
          confirmLabel="Close Campaign"
          tone="red"
          busy={actioningId === confirmCloseId}
          onConfirm={handleConfirmClose}
          onCancel={() => setConfirmCloseId(null)}
        />
      )}

      {confirmDeleteId && (
        <ConfirmModal
          title="Delete campaign?"
          message="Are you sure you want to delete this draft campaign? This can't be undone."
          confirmLabel="Delete Campaign"
          tone="red"
          busy={actioningId === confirmDeleteId}
          onConfirm={handleConfirmDelete}
          onCancel={() => setConfirmDeleteId(null)}
        />
      )}

      {errorMsg && (
        <Toast message={errorMsg} type="error" onDismiss={() => setErrorMsg(null)} />
      )}
    </div>
  )
}
