import type { LoginAs } from './RegisterTypes'

export interface UserDetails {
  code: string
  type: LoginAs
  name: string
  mobile: string
}
