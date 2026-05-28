export type UserStatus = 'active' | 'inactive' | 'pending'

export interface User {
  name: string
  status: UserStatus
}
