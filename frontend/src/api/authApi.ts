import { securityQuestions } from '../data/mockData'
import type { SecurityQuestion } from '../types/ForgotPasswordTypes'
import type { RegisterForm, RegisterResponse } from '../types/RegisterTypes'

export async function fetchSecurityQuestions(): Promise<SecurityQuestion[]> {
  await new Promise(resolve => setTimeout(resolve, 300))
  return securityQuestions
}

export async function registerUser(_form: RegisterForm): Promise<RegisterResponse> {
  await new Promise(resolve => setTimeout(resolve, 600))
  return { success: true, message: 'Account created successfully' }
}

export async function fetchUserSecurityQuestion(_mobile: string): Promise<SecurityQuestion> {
  await new Promise(resolve => setTimeout(resolve, 400))
  // Mock: randomly return one of the two questions the user set at registration
  const userQuestions = securityQuestions.slice(0, 2)
  return userQuestions[Math.floor(Math.random() * userQuestions.length)]
}
