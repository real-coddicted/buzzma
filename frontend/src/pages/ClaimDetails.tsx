import { useEffect, useMemo, useRef, useState } from 'react'
import { useBreadcrumb } from '../contexts/BreadcrumbContext'
import { ClaimProofUnified } from '../components/ui/claim-review/ClaimProofUnified'
import { Toast } from '../components/ui/Toast'
import { fetchCampaignById } from '../api/campaignApi'
import { fetchClaimById, fetchScreenshotUrl, reviewScreenshot, submitClaimReview } from '../api/claimApi'
import { campaignToDeal } from '../api/dealApi'
import { getCurrentUser } from '../api/client'
import { formatExtractedValue, getCampaignValue, getSubmittedValue } from '../components/ui/claim-review/claimUtils'
import type { ClaimReviewItem, Deal } from '../types'
import type { ClaimProofItem, ExtractedField } from '../components/ui/claim-review/ClaimProofGallery'

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

  const [deal, setDeal] = useState<Deal | null>(null)
  const [claimDetail, setClaimDetail] = useState<ClaimReviewItem | null>(null)
  const [claimLoading, setClaimLoading] = useState(true)
  const [proofItems, setProofItems] = useState<ClaimProofItem[]>([])
  const [proofLoading, setProofLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const blobUrlsRef = useRef<string[]>([])

  const userRole = getCurrentUser()?.role
  const isAgency = userRole === 'ROLE_AGENCY'

  useEffect(() => {
    if (!claim.campaignId) return
    let cancelled = false
    fetchCampaignById(claim.campaignId)
      .then(dto => { if (!cancelled) setDeal(campaignToDeal(dto)) })
      .catch(err => { if (!cancelled) setError((err as Error).message || 'Failed to load deal info.') })
    return () => { cancelled = true }
  }, [claim.campaignId])

  useEffect(() => {
    if (!claim.id) { setClaimLoading(false); return }
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
    if (!screenshots || screenshots.length === 0) { setProofItems([]); return }
    let cancelled = false
    setProofLoading(true)
    Promise.all(screenshots.map(s => fetchScreenshotUrl(s.storageKey).then(url => ({ s, url }))))
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
          reviewerComments: s.reviewerComments,
          fields: Object.entries(s.extractedDetails ?? {}).map(([key, sv]): ExtractedField => {
            const raw = sv.extractedValue ?? ''
            const value = formatExtractedValue(key, raw)
            const hasValue = sv.extractedValue != null
            return {
              key,
              label: key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()),
              value,
              matched: hasValue && sv.score != null ? sv.score >= 0.5 : false,
              indeterminate: hasValue && sv.score == null,
              score: sv.score,
              submittedMismatch: sv.mismatch ?? false,
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

  // Enrich proof items with campaign / submitted values once both are loaded
  const enrichedItems = useMemo((): ClaimProofItem[] => {
    const c = claimDetail ?? claim
    return proofItems.map(item => ({
      ...item,
      fields: item.fields.map(f => {
        const campaignValue = deal && f.key ? getCampaignValue(f.key, deal) : undefined
        const submittedValue = f.key ? getSubmittedValue(f.key, c) : undefined
        return { ...f, campaignValue, submittedValue }
      }),
    }))
  }, [proofItems, deal, claimDetail, claim])

  const effectiveClaim = claimDetail ?? claim

  return (
    <div className="max-w-[1600px] mx-auto">
      <ClaimProofUnified
        items={enrichedItems}
        loading={claimLoading || proofLoading}
        isAgency={isAgency}
        userRole={userRole}
        claim={effectiveClaim}
        campaignTitle={deal?.title}
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
        onApproveClaim={comment =>
          submitClaimReview(claim.id, 'APPROVED', comment || undefined)
            .then(updated => setClaimDetail(updated))
            .catch(err => setError((err as Error).message))
        }
        onVerifiedClaim={() =>
          submitClaimReview(claim.id, 'VERIFIED')
            .then(updated => setClaimDetail(updated))
            .catch(err => setError((err as Error).message))
        }
        onRejectClaim={comment =>
          submitClaimReview(claim.id, 'REJECTED', comment)
            .then(updated => setClaimDetail(updated))
            .catch(err => setError((err as Error).message))
        }
      />

      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}
