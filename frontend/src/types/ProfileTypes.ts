import type { LoginAs } from './RegisterTypes'

export interface UserDetails {
  code?: string
  type: LoginAs | 'admin'
  name: string
  mobile: string
  email?: string
}