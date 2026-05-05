export type ConnectionStatus = 'connected' | 'pending' | 'invited'
export type ConnectionType = 'brand' | 'agency'

export interface Connection {
  id: string
  name: string
  type: ConnectionType
  category: string
  status: ConnectionStatus
  since?: string
  avatar: string
  avatarColor: string
}

export type ConnectionSortKey = keyof Pick<Connection, 'name' | 'category' | 'type' | 'status'>
