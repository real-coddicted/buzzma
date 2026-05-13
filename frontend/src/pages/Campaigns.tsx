import { useState } from 'react'
import { Button } from '../components/ui/Button'
import { IconPlus } from '../components/ui/icons'
import { NewCampaignPage } from '../components/ui/campaign/NewCampaignPage'
import { CampaignTable } from '../components/ui/campaign/CampaignTable'
import { CampaignSummaryCards } from '../components/ui/campaign/CampaignSummaryCards'
import type { CampaignRequestDto } from '../types'
import { LaunchCampaignModal } from '../components/ui/campaign/LaunchCampaignModal'
import { CampaignDetailsModal } from '../components/ui/campaign/CampaignDetailsModal'
import { campaigns, linkedEntities, availableEntities } from '../data/mockData'


export function Campaigns() {
  const [showNewCampaign, setShowNewCampaign] = useState(false)
  const [launchModalOpen, setLaunchModalOpen] = useState(false)
  const [detailsModalOpen, setDetailsModalOpen] = useState(false)
  const [selectedCampaignId, setSelectedCampaignId] = useState<string | null>(null)

  const selectedCampaign = campaigns.find(c => c.id === selectedCampaignId)
  const selectedLinkedEntities = selectedCampaignId ? linkedEntities[selectedCampaignId] || [] : []

  function handleCreateCampaign(dto: CampaignRequestDto) {
    console.log('Create campaign:', dto)
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

  const totalBudget   = campaigns.reduce((s, c) => s + c.budget, 0)
  const totalSpent    = campaigns.reduce((s, c) => s + c.spent, 0)
  const totalConv     = campaigns.reduce((s, c) => s + c.conversions, 0)
  const activeCnt     = campaigns.filter(c => c.status === 'active').length

  if (showNewCampaign) {
    return (
      <NewCampaignPage
        onBack={() => setShowNewCampaign(false)}
        onSubmit={handleCreateCampaign}
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
        onViewDetails={handleOpenDetailsModal}
        onLaunch={handleOpenLaunchModal}
      />

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

    </div>
  )
}
