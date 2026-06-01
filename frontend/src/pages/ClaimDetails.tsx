import { useEffect, useState } from 'react'
import { IconChevronRight } from '../components/ui/icons'
import { ClaimDetailsTabs, type ClaimDetailsTab } from '../components/ui/claim-review/ClaimDetailsTabs'
import { ClaimInfo } from '../components/ui/claim-review/ClaimInfo'
import { ClaimProofGallery } from '../components/ui/claim-review/ClaimProofGallery'
import { DealInfo } from '../components/ui/deal/DealInfo'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { fetchCampaignById } from '../api/campaignApi'
import { fetchClaimById } from '../api/claimApi'
import { campaignToDeal } from '../api/dealApi'
import { buildMockProofs } from '../data/mockData'
import type { ClaimReviewItem, Deal } from '../types'

interface ClaimDetailsProps {
  claim: ClaimReviewItem
  onBack: () => void
}

export function ClaimDetails({ claim, onBack }: ClaimDetailsProps) {
  const [tab, setTab] = useState<ClaimDetailsTab>('details')
  const [deal, setDeal] = useState<Deal | null>(null)
  const [dealLoading, setDealLoading] = useState(true)
  const [claimDetail, setClaimDetail] = useState<ClaimReviewItem | null>(null)
  const [claimLoading, setClaimLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!claim.campaignId) {
      setDealLoading(false)
      setError('No campaign linked to this claim.')
      return
    }
    let cancelled = false
    setDealLoading(true)
    fetchCampaignById(claim.campaignId)
      .then(dto => { if (!cancelled) setDeal(campaignToDeal(dto)) })
      .catch(err => { if (!cancelled) setError((err as Error).message || 'Failed to load deal info.') })
      .finally(() => { if (!cancelled) setDealLoading(false) })
    return () => { cancelled = true }
  }, [claim.campaignId])

  useEffect(() => {
    if (!claim.id) {
      setClaimLoading(false)
      return
    }
    let cancelled = false
    setClaimLoading(true)
    fetchClaimById(claim.id)
      .then(data => { if (!cancelled) setClaimDetail(data) })
      .catch(err => { if (!cancelled) setError((err as Error).message || 'Failed to load claim info.') })
      .finally(() => { if (!cancelled) setClaimLoading(false) })
    return () => { cancelled = true }
  }, [claim.id])

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted">
        <button onClick={onBack} className="hover:text-neon-blue transition-colors">
          Claim Review
        </button>
        <IconChevronRight size={12} />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium truncate">
          {claim.orderId}
        </span>
      </div>

      <ClaimDetailsTabs value={tab} onChange={setTab} />

      {tab === 'details' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-14rem)]">
          {dealLoading ? <Loading size={32} className="m-auto" /> : deal ? <DealInfo deal={deal} /> : <div />}
          {claimLoading ? <Loading size={32} className="m-auto" /> : <ClaimInfo claim={{ ...claim, ...claimDetail, mediatorName: claim.mediatorName, campaignName: claim.campaignName }} />}
        </div>
      )}

      {tab === 'proof' && (
        <ClaimProofGallery
          items={buildMockProofs(claim)}
          onApprove={item => console.log('approve', item.id)}
          onRequestProof={item => console.log('request-proof', item.id)}
          onVerified={item => console.log('verified', item.id)}
          onReject={item => console.log('reject', item.id)}
        />
      )}

      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}
