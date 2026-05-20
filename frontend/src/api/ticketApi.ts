import type { components } from '../types/api'
import type { CreateTicketInput, Ticket, TicketActivityEvent, TicketCategory, TicketComment, TicketStatus } from '../types/TicketTypes'
import { fetchWithAuth } from './client'

type TicketCategoryResponseDto = components['schemas']['TicketCategoryResponseDto']
type TicketResponseDto = components['schemas']['TicketResponseDto']
type TicketCommentResponseDto = components['schemas']['TicketCommentResponseDto']
type TicketCommentRequestDto = components['schemas']['TicketCommentRequestDto']
type TicketRequestDto = components['schemas']['TicketRequestDto']

const API_BASE = '/api/v1'

function toDisplayName(value?: string): string {
  if (!value) return 'Unknown'
  const normalized = value.replace(/_/g, ' ').toLowerCase()
  return normalized.charAt(0).toUpperCase() + normalized.slice(1)
}

function mapStatus(status?: TicketResponseDto['status']): TicketStatus {
  switch (status) {
    case 'TICKET_STATUS_ASSIGNED':
    case 'TICKET_STATUS_WAITING_FOR_USER_ACTION':
      return 'InProgress'
    case 'TICKET_STATUS_CLOSED':
      return 'Resolved'
    case 'TICKET_STATUS_OPEN':
    default:
      return 'Open'
  }
}

function mapCategory(dto: TicketCategoryResponseDto): TicketCategory {
  const categoryName = dto.name ?? dto.code ?? 'unknown'
  return {
    id: dto.id ?? '',
    name: categoryName,
    displayName: toDisplayName(categoryName),
    subCategories: (dto.subCategories ?? []).map(sub => {
      const subName = sub.name ?? sub.code ?? 'unknown'
      return {
        id: sub.id ?? '',
        name: subName,
        displayName: toDisplayName(subName),
        requiresOrderId: Boolean(dto.requiresOrderId),
      }
    }),
  }
}

function buildCategoryLookup(categories: TicketCategory[]) {
  const categoryById = new Map(categories.map(category => [category.id, category]))
  const subCategoryById = new Map(
    categories.flatMap(category => category.subCategories.map(subCategory => [subCategory.id, subCategory])),
  )
  return { categoryById, subCategoryById }
}

function mapTicket(dto: TicketResponseDto, categories: TicketCategory[]): Ticket {
  const { categoryById, subCategoryById } = buildCategoryLookup(categories)
  const category = categoryById.get(dto.categoryId ?? '')
  const subCategory = subCategoryById.get(dto.subCategoryId ?? '')

  return {
    id: dto.id ?? '',
    categoryDisplayName: category?.displayName ?? 'Unknown',
    subCategoryDisplayName: subCategory?.displayName ?? 'Unknown',
    orderId: dto.orderId ?? null,
    description: dto.description ?? '',
    status: mapStatus(dto.status),
    createdAt: dto.createdAt ?? new Date().toISOString(),
    updatedAt: dto.updatedAt ?? dto.createdAt ?? new Date().toISOString(),
  }
}

function mapComment(dto: TicketCommentResponseDto): TicketComment {
  return {
    id: dto.id ?? '',
    userId: dto.authorId ?? '',
    userName: dto.authorId ? `User ${dto.authorId.slice(0, 8)}` : 'Support',
    role: 'support',
    message: dto.content ?? '',
    createdAt: dto.createdAt ?? new Date().toISOString(),
  }
}

export async function fetchTicketCategories(): Promise<TicketCategory[]> {
  const res = await fetchWithAuth(`${API_BASE}/ticket-categories`)
  const data = (await res.json()) as TicketCategoryResponseDto[]
  return data.map(mapCategory)
}

export async function fetchMyTickets(): Promise<Ticket[]> {
  const [ticketsRes, categories] = await Promise.all([
    fetchWithAuth(`${API_BASE}/tickets`),
    fetchTicketCategories(),
  ])

  const tickets = (await ticketsRes.json()) as TicketResponseDto[]
  return tickets.map(ticket => mapTicket(ticket, categories))
}

export async function createTicket(input: CreateTicketInput): Promise<Ticket> {
  const [res, categories] = await Promise.all([
    fetchWithAuth(`${API_BASE}/tickets`, {
      method: 'POST',
      body: JSON.stringify({
        categoryId: input.categoryId,
        subCategoryId: input.subCategoryId,
        title: input.title,
        description: input.description,
        orderId: input.orderId,
      } satisfies TicketRequestDto),
    }),
    fetchTicketCategories(),
  ])

  const created = (await res.json()) as TicketResponseDto
  return mapTicket(created, categories)
}

export async function fetchTicketComments(ticketId: string): Promise<TicketComment[]> {
  const res = await fetchWithAuth(`${API_BASE}/tickets/${ticketId}/comments`)
  const data = (await res.json()) as TicketCommentResponseDto[]
  return data.map(mapComment)
}

export async function postTicketComment(ticketId: string, message: string): Promise<TicketComment> {
  const body = {
    ticketId,
    content: message,
  } satisfies TicketCommentRequestDto

  const res = await fetchWithAuth(`${API_BASE}/tickets/${ticketId}/comments`, {
    method: 'POST',
    body: JSON.stringify(body),
  })

  const created = (await res.json()) as TicketCommentResponseDto
  return mapComment(created)
}

export async function fetchTicketActivity(ticketId: string): Promise<TicketActivityEvent[]> {
  const [tickets, comments] = await Promise.all([fetchMyTickets(), fetchTicketComments(ticketId)])
  const ticket = tickets.find(item => item.id === ticketId)

  if (!ticket) return []

  const events: TicketActivityEvent[] = [
    {
      id: `created-${ticket.id}`,
      type: 'created',
      actor: 'System',
      actorRole: 'system',
      description: 'Ticket raised',
      createdAt: ticket.createdAt,
    },
    ...comments.map(comment => ({
      id: `comment-${comment.id}`,
      type: 'comment_added' as const,
      actor: comment.userName,
      actorRole: comment.role,
      description: 'Comment added',
      createdAt: comment.createdAt,
    })),
  ]

  return events.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
}
