export type ConnectionStatus = 'connected' | 'pending' | 'rejected'
export type ConnectionType = 'brand' | 'agency'

export interface Connection {
  id: string
  /** UUID of the other party. */
  toUserId: string
  name: string
  /** Role of the other party, e.g. 'ROLE_MEDIATOR' — used to bucket connections by role. */
  role?: string
  code?: string
  type: ConnectionType
  category: string
  status: ConnectionStatus
  since?: string
  avatar: string
  avatarColor: string
  /** True if the current user owns the invite that created this connection, and can accept/reject it. */
  canApprove: boolean
}

export type ConnectionSortKey = keyof Pick<Connection, 'name' | 'category' | 'type' | 'status'>
