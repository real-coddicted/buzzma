import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ClaimReviewGrid } from '../components/ui/claim-review/ClaimReviewGrid'
import { ClaimReviewCampaignHub } from '../components/ui/claim-review/ClaimReviewCampaignHub'
import { Toast } from '../components/ui/Toast'
import { fetchClaimsToReview, submitClaimReview, bulkApproveClaimReviews, markClaimsReadyForAccounting, mergeReviewedClaim } from '../api/claimApi'
import { fetchCampaignNames, fetchSharedCampaignNames, type CampaignNameOption } from '../api/campaignApi'
import { fetchPublishedCampaignNames } from '../api/dealApi'
import { type ClaimReviewFilters, emptyFilters } from '../components/ui/claim-review/filters/ClaimReviewFilterTypes'
import { getCurrentUser } from '../api/client'
import type { ClaimReviewItem } from '../types'

interface ClaimReviewListProps {
  onViewDetails: (claim: ClaimReviewItem) => void
  onOpenImport: () => void
}

export function ClaimReviewList({ onViewDetails, onOpenImport }: ClaimReviewListProps) {
  const [searchParams] = useSearchParams()
  const seedCampaignId = searchParams.get('campaignId')
  const seedCampaignName = searchParams.get('campaignName')
  const [claims, setClaims] = useState<ClaimReviewItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [totalElements, setTotalElements] = useState(0)
  const [appliedFilters, setAppliedFilters] = useState<ClaimReviewFilters>(() =>
    seedCampaignId ? { ...emptyFilters(), campaignIds: new Set([seedCampaignId]) } : emptyFilters()
  )
  const [campaignOptions, setCampaignOptions] = useState<CampaignNameOption[]>([])

  const loadClaims = useCallback((page = 1) => {
    setLoading(true)
    return fetchClaimsToReview({
      campaignIds: appliedFilters.campaignIds,
      mediatorIds: appliedFilters.mediatorIds,
      brands: appliedFilters.brands,
      platforms: appliedFilters.platforms,
      claimStatuses: appliedFilters.claimStatuses,
      claimCode: appliedFilters.claimCode,
    }, page - 1)
      .then(result => {
        setClaims(result.items)
        setTotalPages(result.totalPages)
        setTotalElements(result.total)
        setCurrentPage(page)
      })
      .catch(err => setError((err as Error).message))
      .finally(() => setLoading(false))
  }, [appliedFilters])

  useEffect(() => { loadClaims() }, [loadClaims])

  function handleApplyFilters(filters: ClaimReviewFilters) {
    setAppliedFilters(filters)
  }

  function handlePageChange(page: number) {
    loadClaims(page)
  }

  // Campaign-hub strip is navigation only (names, no counts) - sourced from the same lightweight
  // /campaigns/names-style endpoint the filter drawer's campaign typeahead already uses, so it adds
  // no new query and no claims payload (unlike a per-campaign pending count, which would require
  // pulling claim rows to compute).
  useEffect(() => {
    const role = getCurrentUser()?.role
    const fetchNames = role === 'ROLE_MEDIATOR' ? fetchPublishedCampaignNames
      : role === 'ROLE_BRAND' ? fetchSharedCampaignNames
      : fetchCampaignNames
    fetchNames()
      .then(setCampaignOptions)
      .catch(() => {}) // best-effort - the hub strip simply stays empty if this fails
  }, [])

  function handleSelectCampaign(campaignId: string | null) {
    setAppliedFilters(prev => ({ ...prev, campaignIds: campaignId ? new Set([campaignId]) : new Set() }))
  }

  function handleApprove(row: ClaimReviewItem, amountApprovedPaise?: number) {
    submitClaimReview(row.id, 'APPROVED', undefined, amountApprovedPaise)
      .then(updated => {
        setClaims(prev => prev.map(c => (c.id === row.id ? mergeReviewedClaim(c, updated) : c)))
      })
      .catch(err => setError((err as Error).message))
  }

  function handleBrandVerify(row: ClaimReviewItem) {
    submitClaimReview(row.id, 'BRAND_VERIFIED')
      .then(updated => {
        setClaims(prev => prev.map(c => (c.id === row.id ? mergeReviewedClaim(c, updated) : c)))
      })
      .catch(err => setError((err as Error).message))
  }

  function handleBulkApprove(selectedClaims: ClaimReviewItem[], approvedAmountsPaise: Record<string, number>): Promise<void> {
    return bulkApproveClaimReviews(selectedClaims.map(c => ({ claimId: c.id, amountApprovedPaise: approvedAmountsPaise[c.id] })))
      .then(updatedList => {
        const updatedById = new Map(updatedList.map(u => [u.id, u]))
        setClaims(prev => prev.map(c => {
          const updated = updatedById.get(c.id)
          return updated ? { ...c, ...updated, campaignName: c.campaignName, mediatorName: c.mediatorName } : c
        }))
      })
      .catch(err => setError((err as Error).message))
  }

  function handleMarkReadyForAccounting(): Promise<number> {
    return markClaimsReadyForAccounting().then(({ updatedCount }) =>
      loadClaims().then(() => updatedCount)
    )
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Claim Review
        </h1>
      </div>
      {campaignOptions.length > 0 && (
        <ClaimReviewCampaignHub
          campaigns={campaignOptions}
          selectedCampaignId={appliedFilters.campaignIds.size === 1 ? [...appliedFilters.campaignIds][0] : null}
          onSelect={handleSelectCampaign}
        />
      )}
      <ClaimReviewGrid
        claims={claims}
        loading={loading}
        total={totalElements}
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
        appliedFilters={appliedFilters}
        onApplyFilters={handleApplyFilters}
        onViewDetails={onViewDetails}
        onApprove={handleApprove}
        onBrandVerify={handleBrandVerify}
        onBulkApprove={handleBulkApprove}
        onOpenImport={onOpenImport}
        onMarkReadyForAccounting={handleMarkReadyForAccounting}
        initialCampaignOption={seedCampaignId && seedCampaignName ? { value: seedCampaignId, label: seedCampaignName } : undefined}
      />
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}