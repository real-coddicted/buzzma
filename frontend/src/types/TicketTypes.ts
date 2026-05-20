export type TicketStatus = 'Open' | 'InProgress' | 'Resolved' | 'Rejected'

export interface Ticket {
  id: string
  categoryDisplayName: string
  subCategoryDisplayName: string
  orderId: string | null
  description: string
  status: TicketStatus
  createdAt: string
  updatedAt: string
}

export interface TicketComment {
  id: string
  userId: string
  userName: string
  role: 'shopper' | 'support' | 'system'
  message: string
  createdAt: string
}

export interface CreateTicketInput {
  categoryId: string
  subCategoryId: string
  title: string
  description: string
  orderId?: string
}

export type TicketActivityType =
  | 'created'
  | 'status_changed'
  | 'comment_added'
  | 'assigned'
  | 'resolved'
  | 'rejected'

export interface TicketActivityEvent {
  id: string
  type: TicketActivityType
  actor: string
  actorRole: 'shopper' | 'support' | 'system'
  description: string
  createdAt: string
}

export interface TicketSubCategory {
  id: string
  name: string
  displayName: string
  requiresOrderId: boolean
}

export interface TicketCategory {
  id: string
  name: string
  displayName: string
  subCategories: TicketSubCategory[]
}
