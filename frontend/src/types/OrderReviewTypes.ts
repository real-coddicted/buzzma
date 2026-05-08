export type OrderStatus = 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled'
export type ReviewStatus = 'pending' | 'in-review' | 'approved' | 'rejected'
export type ApprovalMethod = 'manual' | 'auto'

export interface OrderReviewItem {
  id: string
  campaignName: string
  orderId: string
  orderDate: string
  mediatorName: string
  orderStatus: OrderStatus
  reviewStatus: ReviewStatus
  approvalMethod: ApprovalMethod
  mediatorVerified: boolean
  matchPct: number
}
