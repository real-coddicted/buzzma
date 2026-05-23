import { useState, useEffect } from 'react'
import type { AssignmentItem } from '../../../types/AssignmentTypes'
import { IconChevronRight } from '../icons'
import { DealInfo } from '../deal/DealInfo'
import { AssignmentForm } from './AssignmentForm'
import { fetchCommissionCharged } from '../../../api/assignmentApi'

interface AssignmentDetailProps {
  item: AssignmentItem
  onBack: () => void
  readOnly?: boolean
}

export function AssignmentDetail({ item, onBack, readOnly = false }: AssignmentDetailProps) {
  const [commissionChargedPaise, setCommissionChargedPaise] = useState<number | undefined>(undefined)

  useEffect(() => {
    if (!readOnly) return
    fetchCommissionCharged(item.campaignId)
      .then(setCommissionChargedPaise)
      .catch(() => setCommissionChargedPaise(0))
  }, [readOnly, item.campaignId])
  const deal = {
    id:                   item.id,
    productName:          item.productName,
    productImageUrl:      item.productImageUrl,
    productUrl:           item.productUrl,
    platform:             item.platform,
    platformLabel:        item.platformLabel,
    dealType:             item.dealType,
    dealTypeLabel:        item.dealTypeLabel,
    originalPricePaise:   item.originalPricePaise,
    offeredPricePaise:    item.offeredPricePaise,
    sellerName:           item.sellerName,
    termsAndConditions:   item.termsAndConditions,
    status:               'explore' as const,
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted">
        <button onClick={onBack} className="hover:text-neon-blue transition-colors">
          Assignments
        </button>
        <IconChevronRight size={12} />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium truncate">
          {item.productName}
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-10rem)]">
        <DealInfo deal={deal} />
        <AssignmentForm
          assignmentId={item.id}
          campaignId={item.campaignId}
          basePricePaise={item.offeredPricePaise}
          commissionOfferedPaise={item.commissionOfferedPaise}
          slotsOffered={item.slotsOffered}
          readOnly={readOnly}
          commissionChargedPaise={commissionChargedPaise}
          onPublished={onBack}
        />
      </div>
    </div>
  )
}
