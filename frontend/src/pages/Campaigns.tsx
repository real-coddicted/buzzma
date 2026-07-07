import { useState, useEffect, useCallback } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Button } from '../components/ui/Button'
import { IconPlus } from '../components/ui/icons'
import { NewCampaignPage } from '../components/ui/campaign/NewCampaignPage'
import { CampaignTable } from '../components/ui/campaign/CampaignTable'
import { CampaignSummaryCards } from '../components/ui/campaign/CampaignSummaryCards'
import { Loading } from '../components/ui/Loading'
import type { Campaign, CampaignRequestDto, Platform, CampaignType } from '../types'
import { createCampaign, updateCampaign, fetchCampaigns, fetchCampaignById, copyCampaign, pauseCampaign, resumeCampaign, closeCampaign, yyyymmddToIso, type CampaignResponseDto } from '../api/campaignApi'
import type { CampaignForm } from '../components/ui/campaign/campaignFormConstants'
import { paiseToRupees } from '../utils/currency'
import { Toast } from '../components/ui/Toast'
import { ConfirmModal } from '../components/ui/ConfirmModal'
import { useSSE } from '../hooks/useSSE'


function responseToForm(dto: CampaignResponseDto): CampaignForm {
  const assignments = dto.assignments ?? []
  return {
    title: dto.title ?? '',
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
    assignees: assignments.map(a => ({
      id: a.assigneeId ?? '',
      name: a.assigneeName ?? '',
      slotsAvailable: a.slotOffered ?? 0,
      commissionOffered: paiseToRupees(a.commissionOfferedPaise ?? 0),
    })),
    termsAndConditions: dto.termsAndConditions ?? '',
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

  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [loading, setLoading] = useState(true)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [detail, setDetail] = useState<CampaignDetail | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [confirmPauseId, setConfirmPauseId] = useState<string | null>(null)
  const [confirmCloseId, setConfirmCloseId] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)

  const loadCampaigns = useCallback(() => {
    setLoading(true)
    fetchCampaigns()
      .then(setCampaigns)
      .catch(err => setErrorMsg((err as Error).message || 'Failed to load campaigns.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadCampaigns() }, [loadCampaigns])

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
      loadCampaigns()
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to copy campaign.')
    }
  }

  function handleViewCampaign(id: string) {
    setSearchParams({ view: 'view', id })
  }

  function handleRequestClose(id: string) {
    setConfirmCloseId(id)
  }

  async function handleConfirmClose() {
    if (!confirmCloseId) return
    setActioningId(confirmCloseId)
    try {
      await closeCampaign(confirmCloseId)
      loadCampaigns()
      setConfirmCloseId(null)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to close campaign.')
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
      loadCampaigns()
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
      loadCampaigns()
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to resume campaign.')
    }
  }

  function handleBack() {
    navigate(-1)
  }

  async function handleCreateCampaign(dto: CampaignRequestDto): Promise<void> {
    await createCampaign(dto)
    handleBack()
    loadCampaigns()
  }

  async function handleUpdateCampaign(dto: CampaignRequestDto): Promise<void> {
    if (!campaignId || !detail?.status) return
    await updateCampaign(campaignId, dto, detail.status)
    handleBack()
    loadCampaigns()
  }

  const totalSpent  = campaigns.reduce((s, c) => s + c.spent, 0)
  const totalConv   = campaigns.reduce((s, c) => s + c.conversions, 0)
  const activeCnt   = campaigns.filter(c => c.status === 'active').length

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
        readOnly={isViewMode}
        campaignCode={detail?.code ?? undefined}
      />
    )
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Campaigns
          </h1>
        </div>
        <Button variant="primary" size="md" leftIcon={<IconPlus size={14} />} onClick={() => setSearchParams({ view: 'new' })}>
          New Campaign
        </Button>
      </div>

      <CampaignSummaryCards
        totalSpent={totalSpent}
        totalConversions={totalConv}
        activeCount={activeCnt}
      />

      <CampaignTable
        campaigns={campaigns}
        loading={loading}
        onEdit={handleEditCampaign}
        onCopy={handleCopyCampaign}
        onView={handleViewCampaign}
        onPause={handleRequestPause}
        onResume={handleResumeCampaign}
        onClose={handleRequestClose}
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

      {errorMsg && (
        <Toast message={errorMsg} type="error" onDismiss={() => setErrorMsg(null)} />
      )}
    </div>
  )
}
