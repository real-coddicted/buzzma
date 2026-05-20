import { securityQuestions } from '../data/mockData'
import type { SecurityQuestion } from '../types/ForgotPasswordTypes'
import type { RegisterForm, RegisterResponse } from '../types/RegisterTypes'
import type { LoginForm, LoginResponse } from '../types/LoginTypes'
import type { components } from '../types/api'
import { clearCurrentUser, setAccessToken, setCurrentUser } from './client'

type SignInResponse = components['schemas']['UserSignInResponseDto']

export async function fetchSecurityQuestions(): Promise<SecurityQuestion[]> {
  await new Promise(resolve => setTimeout(resolve, 300))
  return securityQuestions
}

export async function loginUser(form: LoginForm): Promise<LoginResponse> {
  const res = await fetch('/api/v1/auth/sign-in', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mobile: form.mobile, password: form.password }),
  })
  if (!res.ok) {
    const message = res.status === 401 ? 'Invalid mobile number or password' : 'Something went wrong. Please try again.'
    return { success: false, message }
  }
  const data: SignInResponse = await res.json()
  if (data.tokens?.accessToken) {
    setAccessToken(data.tokens.accessToken)
    if (data.userSummary) {
      setCurrentUser(data.userSummary)
    } else {
      clearCurrentUser()
    }
  }
  return { success: true, message: '' }
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
