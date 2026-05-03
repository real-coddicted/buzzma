import type { LoginAs } from './RegisterTypes'

export type { LoginAs }

export type SecurityQuestion = string

export interface ForgotPasswordForm {
  role: LoginAs
  mobile: string
  securityQuestion: SecurityQuestion
  answer: string
}

export interface ResetPasswordForm {
  password: string
  confirmPassword: string
}
