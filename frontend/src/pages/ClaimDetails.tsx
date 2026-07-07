import { useEffect, useRef, useState } from 'react'
import { ClaimDetailsTabs, type ClaimDetailsTab } from '../components/ui/claim-review/ClaimDetailsTabs'
import { useBreadcrumb } from '../contexts/BreadcrumbContext'
import { ClaimInfo } from '../components/ui/claim-review/ClaimInfo'
import { ClaimProofGallery, type ClaimProofItem } from '../components/ui/claim-review/ClaimProofGallery'
import { ClaimProofActions } from '../components/ui/claim-review/ClaimProofActions'
import { DealInfo } from '../components/ui/deal/DealInfo'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { fetchCampaignById } from '../api/campaignApi'
import { fetchClaimById, fetchScreenshotUrl, reviewScreenshot, submitClaimReview } from '../api/claimApi'
import { campaignToDeal } from '../api/dealApi'
import { getCurrentUser } from '../api/client'
import { PLATFORM_LABELS } from '../constants/campaigns'
import type { Platform } from '../types'
import type { ClaimReviewItem, Deal } from '../types'

interface ClaimDetailsProps {
  claim: ClaimReviewItem
  onBack: () => void
}

export function ClaimDetails({ claim, onBack }: ClaimDetailsProps) {
  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(claim.orderId, onBack)
    return clearDetail
  }, [claim.orderId, onBack, setDetail, clearDetail])

  const [tab, setTab] = useState<ClaimDetailsTab>('proof')
  const [deal, setDeal] = useState<Deal | null>(null)
  const [dealLoading, setDealLoading] = useState(true)
  const [claimDetail, setClaimDetail] = useState<ClaimReviewItem | null>(null)
  const [claimLoading, setClaimLoading] = useState(true)
  const [proofItems, setProofItems] = useState<ClaimProofItem[]>([])
  const [proofLoading, setProofLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const blobUrlsRef = useRef<string[]>([])

  const userRole = getCurrentUser()?.role
  const isAgency = userRole === 'ROLE_AGENCY'

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

  useEffect(() => {
    const screenshots = claimDetail?.screenshots
    if (!screenshots || screenshots.length === 0) {
      setProofItems([])
      return
    }
    let cancelled = false
    setProofLoading(true)
    Promise.all(
      screenshots.map(s =>
        fetchScreenshotUrl(s.storageKey).then(url => ({ s, url }))
      )
    )
      .then(results => {
        if (cancelled) return
        const prevUrls = blobUrlsRef.current
        const nextUrls = results.map(r => r.url)
        prevUrls.forEach(u => URL.revokeObjectURL(u))
        blobUrlsRef.current = nextUrls
        setProofItems(results.map(({ s, url }, i) => ({
          id: s.id || `screenshot-${i}`,
          imageUrl: url,
          imageAlt: `Screenshot ${i + 1}`,
          type: s.type,
          score: s.score,
          verificationStatus: s.verificationStatus,
          fields: Object.entries(s.extractedDetails ?? {}).map(([key, sv]) => {
            const raw = sv.extractedValue ?? ''
            const value = key === 'platform' ? (PLATFORM_LABELS[raw as Platform] ?? raw) : raw
            const hasValue = sv.extractedValue != null
            return {
              label: key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()),
              value,
              matched: hasValue && sv.score != null ? sv.score >= 0.5 : false,
              indeterminate: hasValue && sv.score == null,
              score: sv.score,
            }
          }),
        })))
      })
      .catch(err => { if (!cancelled) setError((err as Error).message || 'Failed to load screenshots.') })
      .finally(() => { if (!cancelled) setProofLoading(false) })
    return () => { cancelled = true }
  }, [claimDetail?.screenshots])

  useEffect(() => {
    const urls = blobUrlsRef.current
    return () => { urls.forEach(u => URL.revokeObjectURL(u)) }
  }, [])

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <ClaimDetailsTabs value={tab} onChange={setTab} />

      {tab === 'details' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-14rem)]">
          {dealLoading ? <Loading size={32} className="m-auto" /> : deal ? <DealInfo deal={deal} /> : <div />}
          {claimLoading ? <Loading size={32} className="m-auto" /> : <ClaimInfo claim={{ ...claim, ...claimDetail, mediatorName: claim.mediatorName, campaignName: claim.campaignName }} />}
        </div>
      )}

      {tab === 'proof' && (
        proofLoading
          ? <Loading size={32} className="m-auto" />
          : <ClaimProofGallery
              items={proofItems}
              isAgency={isAgency}
              onApproveScreenshot={item =>
                reviewScreenshot(item.id, claim.id, 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED')
                  .then(updated => setClaimDetail(updated))
                  .catch(err => setError((err as Error).message))
              }
              onRejectScreenshot={(item, comment) =>
                reviewScreenshot(item.id, claim.id, 'SCREENSHOT_VERIFICATION_STATUS_REJECTED', comment)
                  .then(updated => setClaimDetail(updated))
                  .catch(err => setError((err as Error).message))
              }
            />
      )}

      <ClaimProofActions
        userRole={userRole}
        isUnderReview={claimDetail?.isUnderReview ?? false}
        mediatorVerified={claimDetail?.mediatorVerified ?? false}
        onApprove={comment =>
          submitClaimReview(claim.id, 'APPROVED', comment || undefined)
            .then(updated => setClaimDetail(updated))
            .catch(err => setError((err as Error).message))
        }
        onVerified={() =>
          submitClaimReview(claim.id, 'VERIFIED')
            .then(updated => setClaimDetail(updated))
            .catch(err => setError((err as Error).message))
        }
        onReject={comment =>
          submitClaimReview(claim.id, 'REJECTED', comment)
            .then(updated => setClaimDetail(updated))
            .catch(err => setError((err as Error).message))
        }
      />

      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}
