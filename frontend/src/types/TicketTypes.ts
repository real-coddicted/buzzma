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
  role: string
  message: string
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
