import { describe, it, expect } from 'vitest'
import { mapTicket } from './ticketApi'
import type { components } from '../types/api'

type TicketResponseDto = components['schemas']['TicketResponseDto']

function makeDto(overrides: Partial<TicketResponseDto> = {}): TicketResponseDto {
  return {
    id: 'ticket-1',
    code: 'H9GT-W8E6',
    categoryId: 'cat-1',
    subCategoryId: 'sub-1',
    description: 'test description',
    status: 'TICKET_STATUS_IN_PROGRESS',
    ...overrides,
  }
}

describe('mapTicket', () => {
  it('resolves category/subCategory display names from the DTO instead of a role-scoped lookup', () => {
    const ticket = mapTicket(makeDto({ categoryName: 'Claim', subCategoryName: 'Product Issue' }))
    expect(ticket.categoryDisplayName).toBe('Claim')
    expect(ticket.subCategoryDisplayName).toBe('Product issue')
  })

  it('falls back to Unknown when the backend omits category/subCategory names', () => {
    const ticket = mapTicket(makeDto())
    expect(ticket.categoryDisplayName).toBe('Unknown')
    expect(ticket.subCategoryDisplayName).toBe('Unknown')
  })

  it('carries the ticket title through', () => {
    const ticket = mapTicket(makeDto({ title: 'Order not delivered' }))
    expect(ticket.title).toBe('Order not delivered')
  })

  it('leaves title undefined when absent', () => {
    const ticket = mapTicket(makeDto())
    expect(ticket.title).toBeUndefined()
  })
})