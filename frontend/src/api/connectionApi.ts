import type { Connection, ConnectionStatus } from '../types/ConnectionTypes'
import { connections } from '../data/mockData'

export interface ConnectionSummary {
  total: number
  connectedCount: number
  pendingCount: number
}

export async function fetchConnections(): Promise<Connection[]> {
  await new Promise(resolve => setTimeout(resolve, 400))
  return connections
}

export async function fetchInviteCode(): Promise<string> {
  await new Promise(resolve => setTimeout(resolve, 600))
  return 'INV-MOCK-2026-XK9P'
}

export async function fetchConnectionSummary(): Promise<ConnectionSummary> {
  await new Promise(resolve => setTimeout(resolve, 300))
  return {
    total:          connections.length,
    connectedCount: connections.filter((c): c is Connection & { status: ConnectionStatus } => c.status === 'connected').length,
    pendingCount:   connections.filter(c => c.status === 'pending').length,
  }
}
