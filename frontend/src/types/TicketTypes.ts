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
