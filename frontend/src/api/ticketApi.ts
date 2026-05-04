import type { TicketCategory } from '../types/TicketTypes'

const MOCK_CATEGORIES: TicketCategory[] = [
  {
    id: 'cat-1',
    name: 'order_issue',
    displayName: 'Order Issue',
    subCategories: [
      { id: 'sub-1-1', name: 'missing_item',     displayName: 'Missing Item',          requiresOrderId: true },
      { id: 'sub-1-2', name: 'wrong_item',        displayName: 'Wrong Item Received',   requiresOrderId: true },
      { id: 'sub-1-3', name: 'damaged_item',      displayName: 'Damaged Item',          requiresOrderId: true },
      { id: 'sub-1-4', name: 'order_not_arrived', displayName: 'Order Not Arrived',     requiresOrderId: true },
    ],
  },
  {
    id: 'cat-2',
    name: 'payment',
    displayName: 'Payment & Billing',
    subCategories: [
      { id: 'sub-2-1', name: 'refund_request',  displayName: 'Refund Request',   requiresOrderId: true },
      { id: 'sub-2-2', name: 'double_charged',  displayName: 'Double Charged',   requiresOrderId: true },
      { id: 'sub-2-3', name: 'payment_failed',  displayName: 'Payment Failed',   requiresOrderId: false },
      { id: 'sub-2-4', name: 'invoice_request', displayName: 'Invoice Request',  requiresOrderId: false },
    ],
  },
  {
    id: 'cat-3',
    name: 'account',
    displayName: 'Account & Access',
    subCategories: [
      { id: 'sub-3-1', name: 'login_issue',       displayName: 'Login Issue',         requiresOrderId: false },
      { id: 'sub-3-2', name: 'account_suspended', displayName: 'Account Suspended',   requiresOrderId: false },
      { id: 'sub-3-3', name: 'update_details',    displayName: 'Update My Details',   requiresOrderId: false },
    ],
  },
  {
    id: 'cat-4',
    name: 'campaign',
    displayName: 'Campaign',
    subCategories: [
      { id: 'sub-4-1', name: 'campaign_not_visible', displayName: 'Campaign Not Visible', requiresOrderId: false },
      { id: 'sub-4-2', name: 'commission_dispute',   displayName: 'Commission Dispute',   requiresOrderId: true },
      { id: 'sub-4-3', name: 'slot_issue',           displayName: 'Slot Issue',           requiresOrderId: false },
    ],
  },
  {
    id: 'cat-5',
    name: 'other',
    displayName: 'Other',
    subCategories: [
      { id: 'sub-5-1', name: 'general_inquiry', displayName: 'General Inquiry', requiresOrderId: false },
      { id: 'sub-5-2', name: 'feedback',        displayName: 'Feedback',        requiresOrderId: false },
    ],
  },
]

export async function fetchTicketCategories(): Promise<TicketCategory[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return MOCK_CATEGORIES
}
