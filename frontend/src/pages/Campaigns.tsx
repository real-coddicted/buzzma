import { useState, useEffect, useCallback } from 'react'
import { Button } from '../components/ui/Button'
import { IconPlus } from '../components/ui/icons'
import { NewCampaignPage } from '../components/ui/campaign/NewCampaignPage'
import { CampaignTable } from '../components/ui/campaign/CampaignTable'
import { CampaignSummaryCards } from '../components/ui/campaign/CampaignSummaryCards'
import type { Campaign, CampaignRequestDto, Platform, CampaignType } from '../types'
import { createCampaign, updateCampaign, fetchCampaigns, fetchCampaignById, publishCampaign, copyCampaign, yyyymmddToIso, type CampaignResponseDto } from '../api/campaignApi'
import type { CampaignForm } from '../components/ui/campaign/campaignFormConstants'
import { Toast } from '../components/ui/Toast'
import { useSSE } from '../hooks/useSSE'


function responseToForm(dto: CampaignResponseDto): CampaignForm {
  const paisToRupees = (p?: number) => (p != null ? (p / 100).toString() : '')
  const assignments = dto.assignments ?? []
  return {
    title: dto.title ?? '',
    platform: (dto.platform ?? '') as Platform | '',
    productBrandName: dto.productBrandName ?? '',
    productName: dto.productName ?? '',
    productImageUrl: dto.productImageUrl ?? '',
    productUrl: dto.productLink ?? '',
    sellerName: dto.sellerName ?? '',
    originalPriceRupees: paisToRupees(dto.productPricePaise),
    campaignPriceRupees: paisToRupees(dto.campaignPricePaise),
    commissionRupees: '',
    returnWindowDays: dto.returnWindowDays?.toString() ?? '',
    campaignType: (dto.campaignType ?? '') as CampaignType | '',
    startDate: yyyymmddToIso(dto.startDate),
    endDate: yyyymmddToIso(dto.endDate),
    totalSlots: dto.totalSlots?.toString() ?? '',
    openToAll: assignments.length === 0,
    assignees: assignments.map(a => ({
      id: a.assigneeId ?? '',
      name: a.assigneeId ?? '',
      slotsAvailable: a.slotOffered ?? 0,
      commissionOffered: (a.commissionOfferedPaise ?? 0) / 100,
    })),
    termsAndConditions: dto.termsAndConditions ?? '',
  }
}

export function Campaigns() {
  const [showNewCampaign, setShowNewCampaign] = useState(false)
  const [editingForm, setEditingForm] = useState<CampaignForm | null>(null)
  const [editingCampaignId, setEditingCampaignId] = useState<string | null>(null)
  const [editingCampaignStatus, setEditingCampaignStatus] = useState<NonNullable<CampaignResponseDto['status']> | null>(null)
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [loading, setLoading] = useState(true)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  const loadCampaigns = useCallback(() => {
    setLoading(true)
    fetchCampaigns()
      .then(setCampaigns)
      .catch(err => setErrorMsg((err as Error).message || 'Failed to load campaigns.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadCampaigns() }, [loadCampaigns])

  useSSE('EVENT_TYPE_REFRESH', loadCampaigns, 'campaigns')

  async function handleCopyCampaign(id: string) {
    try {
      await copyCampaign(id)
      loadCampaigns()
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to copy campaign.')
    }
  }

  async function handleEditCampaign(id: string) {
    try {
      const dto = await fetchCampaignById(id)
      setEditingCampaignId(id)
      setEditingCampaignStatus(dto.status ?? 'CAMPAIGN_STATUS_DRAFT')
      setEditingForm(responseToForm(dto))
      setShowNewCampaign(true)
    } catch (err) {
      setErrorMsg((err as Error).message || 'Failed to load campaign.')
    }
  }

  async function handleLaunchFromEdit() {
    if (!editingCampaignId) return
    await publishCampaign(editingCampaignId)
    handleBack()
    loadCampaigns()
  }

  function handleBack() {
    setShowNewCampaign(false)
    setEditingForm(null)
    setEditingCampaignId(null)
    setEditingCampaignStatus(null)
  }

  async function handleCreateCampaign(dto: CampaignRequestDto): Promise<void> {
    await createCampaign(dto)
    handleBack()
    loadCampaigns()
  }

  async function handleUpdateCampaign(dto: CampaignRequestDto): Promise<void> {
    if (!editingCampaignId || !editingCampaignStatus) return
    await updateCampaign(editingCampaignId, dto, editingCampaignStatus)
    handleBack()
    loadCampaigns()
  }

  const totalBudget = campaigns.reduce((s, c) => s + c.budget, 0)
  const totalSpent  = campaigns.reduce((s, c) => s + c.spent, 0)
  const totalConv   = campaigns.reduce((s, c) => s + c.conversions, 0)
  const activeCnt   = campaigns.filter(c => c.status === 'active').length

  if (showNewCampaign) {
    return (
      <NewCampaignPage
        onBack={handleBack}
        onSubmit={editingCampaignId ? handleUpdateCampaign : handleCreateCampaign}
        onLaunch={editingCampaignId ? handleLaunchFromEdit : undefined}
        initialForm={editingForm ?? undefined}
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
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {campaigns.length} total campaigns · {activeCnt} active
          </p>
        </div>
        <Button variant="primary" size="md" leftIcon={<IconPlus size={14} />} onClick={() => setShowNewCampaign(true)}>
          New Campaign
        </Button>
      </div>

      <CampaignSummaryCards
        totalBudget={totalBudget}
        totalSpent={totalSpent}
        totalConversions={totalConv}
        activeCount={activeCnt}
      />

      <CampaignTable
        campaigns={campaigns}
        loading={loading}
        onEdit={handleEditCampaign}
        onCopy={handleCopyCampaign}
      />

      {errorMsg && (
        <Toast message={errorMsg} type="error" onDismiss={() => setErrorMsg(null)} />
      )}
    </div>
  )
}
