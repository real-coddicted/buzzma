import { securityQuestions } from '../data/mockData'
import type { SecurityQuestion } from '../types/ForgotPasswordTypes'
import type { RegisterForm, RegisterResponse } from '../types/RegisterTypes'
import type { LoginForm, LoginResponse } from '../types/LoginTypes'

export async function fetchSecurityQuestions(): Promise<SecurityQuestion[]> {
  await new Promise(resolve => setTimeout(resolve, 300))
  return securityQuestions
}

export async function loginUser(_form: LoginForm): Promise<LoginResponse> {
  await new Promise(resolve => setTimeout(resolve, 500))
  // Mock: succeed for any input — swap for a real fetch when the backend is ready
  return { success: false, message: 'Login successful' }
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
