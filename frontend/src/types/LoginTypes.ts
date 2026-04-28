import type { LoginAs } from './RegisterTypes'

export type { LoginAs }

export interface LoginForm {
  loginAs: LoginAs
  mobile: string
  password: string
}
