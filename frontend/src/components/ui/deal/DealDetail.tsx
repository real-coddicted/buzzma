import { useEffect } from 'react'
import type { Deal } from '../../../types/DealTypes'
import { DealInfo } from './DealInfo'
import { ClaimDeal } from './ClaimDeal'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'

interface DealDetailProps {
  deal: Deal
  onBack: () => void
}

export function DealDetail({ deal, onBack }: DealDetailProps) {
  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(deal.productName, onBack)
    return clearDetail
  }, [deal.productName, onBack, setDetail, clearDetail])

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 lg:h-[calc(100vh-10rem)]">
        <DealInfo deal={deal} />
        <ClaimDeal deal={deal} />
      </div>
    </div>
  )
}
