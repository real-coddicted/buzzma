import type { LoginAs } from './RegisterTypes'

export type { LoginAs }

export type SecurityQuestion = string

export interface ForgotPasswordForm {
  mobile: string
  questionId: string
  securityQuestion: SecurityQuestion
  answer: string
}

export interface PasswordResetChallenge {
  mobile: string
  questionId: string
  answer: string
}

export type UserSecurityQuestionResult =
  | { success: true; questionId: string; question: SecurityQuestion }
  | { success: false; message: string }

export interface PasswordResetResult {
  success: boolean
  message: string
}

export interface ResetPasswordForm {
  password: string
  confirmPassword: string
}
