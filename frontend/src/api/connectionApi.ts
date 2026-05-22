import type { components, operations } from '../types/api'
import type { Connection, ConnectionStatus } from '../types/ConnectionTypes'
import { fetchWithAuth } from './client'

type ConnectionResponseDto = components['schemas']['ConnectionResponseDto']
type ConnectionRequestDto = components['schemas']['ConnectionRequestDto']
type ConnectionSummaryResponseDto = components['schemas']['ConnectionSummaryResponseDto']
type InviteRequestDto = components['schemas']['InviteRequestDto']
type InviteResponseDto = components['schemas']['InviteResponseDto']

/** Accept/reject action values accepted by POST /connections/action/{action}. */
export type ConnectionAction = operations['action']['parameters']['path']['action']

const API_BASE = '/api/v1'

const AVATAR_COLORS = ['#57c7ff', '#bd93f9', '#50fa7b', '#ffb86c', '#ff79c6', '#ff5555', '#8be9fd']

export interface ConnectionSummary {
  total: number
  connectedCount: number
  pendingCount: number
}

function mapStatus(status?: string): ConnectionStatus {
  switch (status) {
    case 'CONNECTION_STATUS_REQUESTED':
      return 'pending'
    case 'CONNECTION_STATUS_REJECTED':
      return 'rejected'
    case 'CONNECTION_STATUS_ACCEPTED':
    default:
      return 'connected'
  }
}

function pickColor(seed: string): string {
  let hash = 0
  for (let i = 0; i < seed.length; i += 1) {
    hash = (hash * 31 + seed.charCodeAt(i)) >>> 0
  }
  return AVATAR_COLORS[hash % AVATAR_COLORS.length]
}

function formatSince(iso?: string): string | undefined {
  if (!iso) return undefined
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return undefined
  return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
}

function mapConnection(dto: ConnectionResponseDto): Connection {
  const toUserId = dto.toUserId ?? ''
  const userId = toUserId || dto.fromUserId || ''
  const name = userId ? `User ${userId.slice(0, 8)}` : 'Unknown'
  return {
    id: dto.id ?? '',
    toUserId,
    name,
    type: 'brand',
    category: '',
    status: mapStatus(dto.status),
    since: formatSince(dto.createdAt),
    avatar: (userId.charAt(0) || '?').toUpperCase(),
    avatarColor: pickColor(userId || name),
  }
}

const BACKEND_STATUS: Record<ConnectionStatus, string> = {
  connected: 'CONNECTION_STATUS_ACCEPTED',
  pending: 'CONNECTION_STATUS_REQUESTED',
  rejected: 'CONNECTION_STATUS_REJECTED',
}

export async function fetchConnections(filter: ConnectionStatus | 'all'): Promise<Connection[]> {
  const query = filter === 'all' ? '' : `?status=${BACKEND_STATUS[filter]}`
  const res = await fetchWithAuth(`${API_BASE}/connections${query}`)
  const data = (await res.json()) as ConnectionResponseDto[]
  return data.map(mapConnection)
}

/** Accept or reject a pending connection request addressed to `toUserId`. */
export async function actionConnection(toUserId: string, action: ConnectionAction): Promise<void> {
  const body: ConnectionRequestDto = { toUserId }
  await fetchWithAuth(`${API_BASE}/connections/action/${action}`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

/** Delete (soft-delete) a connection by its id. */
export async function deleteConnection(id: string): Promise<void> {
  await fetchWithAuth(`${API_BASE}/connections/${id}`, { method: 'DELETE' })
}

/** Generate a fresh invite code for the current user via POST /invites. */
export async function fetchInviteCode(): Promise<string> {
  const body: InviteRequestDto = {}
  const res = await fetchWithAuth(`${API_BASE}/invites`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  const data = (await res.json()) as InviteResponseDto
  return data.code ?? ''
}

export async function fetchConnectionSummary(): Promise<ConnectionSummary> {
  const res = await fetchWithAuth(`${API_BASE}/connections/summary`)
  const data = (await res.json()) as ConnectionSummaryResponseDto
  return {
    total:          data.total ?? 0,
    connectedCount: data.connected ?? 0,
    pendingCount:   data.pending ?? 0,
  }
}
