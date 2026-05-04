import type { Ticket, TicketCategory, TicketComment } from '../types/TicketTypes'

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

const MOCK_TICKETS: Ticket[] = [
  {
    id: 'tkt-001',
    categoryDisplayName: 'Order Issue',
    subCategoryDisplayName: 'Missing Item',
    orderId: 'ORD-100234',
    description: 'I placed an order last week but one of the items was missing from the delivery.',
    status: 'Open',
    createdAt: '2026-04-20T10:30:00Z',
    updatedAt: '2026-04-20T10:30:00Z',
  },
  {
    id: 'tkt-002',
    categoryDisplayName: 'Payment & Billing',
    subCategoryDisplayName: 'Refund Request',
    orderId: 'ORD-99871',
    description: 'I returned the product two weeks ago but have not received the refund yet.',
    status: 'InProgress',
    createdAt: '2026-04-15T08:14:00Z',
    updatedAt: '2026-04-18T14:22:00Z',
  },
  {
    id: 'tkt-003',
    categoryDisplayName: 'Account & Access',
    subCategoryDisplayName: 'Login Issue',
    orderId: null,
    description: 'Unable to log in after changing my phone number.',
    status: 'Resolved',
    createdAt: '2026-04-10T17:45:00Z',
    updatedAt: '2026-04-12T09:00:00Z',
  },
  {
    id: 'tkt-004',
    categoryDisplayName: 'Campaign',
    subCategoryDisplayName: 'Commission Dispute',
    orderId: 'ORD-88820',
    description: 'Commission for order ORD-88820 was not credited to my account after campaign completion.',
    status: 'Rejected',
    createdAt: '2026-04-05T12:00:00Z',
    updatedAt: '2026-04-07T16:30:00Z',
  },
  {
    id: 'tkt-005',
    categoryDisplayName: 'Order Issue',
    subCategoryDisplayName: 'Damaged Item',
    orderId: 'ORD-112045',
    description: 'The product arrived with visible damage to the packaging and the item inside.',
    status: 'Open',
    createdAt: '2026-04-25T09:00:00Z',
    updatedAt: '2026-04-25T09:00:00Z',
  },
]

export async function fetchMyTickets(): Promise<Ticket[]> {
  await new Promise(resolve => setTimeout(resolve, 500))
  return MOCK_TICKETS
}

const MOCK_COMMENTS: Record<string, TicketComment[]> = {
  'tkt-001': [
    { id: 'c-1-1', userId: 'u-1', userName: 'Support Agent', role: 'support', message: 'Hi, could you please share the order confirmation number?', createdAt: '2026-04-20T11:00:00Z' },
    { id: 'c-1-2', userId: 'u-2', userName: 'Alex Rivera',   role: 'shopper', message: 'Sure, it is ORD-100234. The missing item was the phone case.', createdAt: '2026-04-20T11:45:00Z' },
    { id: 'c-1-3', userId: 'u-1', userName: 'Support Agent', role: 'support', message: "We've raised a replacement request. You'll receive it within 3–5 business days.", createdAt: '2026-04-21T09:10:00Z' },
  ],
  'tkt-002': [
    { id: 'c-2-1', userId: 'u-1', userName: 'Support Agent', role: 'support', message: 'We can see the return was received. The refund is being processed.', createdAt: '2026-04-16T10:00:00Z' },
    { id: 'c-2-2', userId: 'u-2', userName: 'Alex Rivera',   role: 'shopper', message: 'How long will it take to reflect in my account?', createdAt: '2026-04-16T10:30:00Z' },
    { id: 'c-2-3', userId: 'u-1', userName: 'Support Agent', role: 'support', message: 'Typically 5–7 business days depending on your bank.', createdAt: '2026-04-16T11:00:00Z' },
  ],
  'tkt-003': [
    { id: 'c-3-1', userId: 'u-1', userName: 'Support Agent', role: 'support', message: 'Your account has been unlinked from the old number. Please try logging in with OTP on the new number.', createdAt: '2026-04-11T08:00:00Z' },
    { id: 'c-3-2', userId: 'u-2', userName: 'Alex Rivera',   role: 'shopper', message: "It's working now, thank you!", createdAt: '2026-04-11T08:30:00Z' },
  ],
  'tkt-004': [],
  'tkt-005': [
    { id: 'c-5-1', userId: 'u-1', userName: 'Support Agent', role: 'support', message: 'Please share a photo of the damaged packaging so we can escalate this.', createdAt: '2026-04-25T10:00:00Z' },
  ],
}

export async function fetchTicketComments(ticketId: string): Promise<TicketComment[]> {
  await new Promise(resolve => setTimeout(resolve, 350))
  return MOCK_COMMENTS[ticketId] ?? []
}

export async function postTicketComment(ticketId: string, message: string): Promise<TicketComment> {
  await new Promise(resolve => setTimeout(resolve, 400))
  const comment: TicketComment = {
    id: `c-${Date.now()}`,
    userId: 'u-2',
    userName: 'Alex Rivera',
    role: 'shopper',
    message,
    createdAt: new Date().toISOString(),
  }
  if (!MOCK_COMMENTS[ticketId]) {
    MOCK_COMMENTS[ticketId] = []
  }
  MOCK_COMMENTS[ticketId].push(comment)
  return comment
}
