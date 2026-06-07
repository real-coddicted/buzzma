import { securityQuestions } from '../data/mockData'
import type { SecurityQuestion } from '../types/ForgotPasswordTypes'
import type { RegisterForm, RegisterResponse } from '../types/RegisterTypes'
import type { LoginForm, LoginResponse } from '../types/LoginTypes'
import type { components } from '../types/api'
import { clearCurrentUser, setAccessToken, setCurrentUser } from './client'

type SignInResponse = components['schemas']['UserSignInResponseDto']
type UserRegistrationRequestDto = components['schemas']['UserRegistrationRequestDto']
type SecurityQuestionWrapper = components['schemas']['SecurityQuestionWrapper']
type BackendSecurityQuestion = components['schemas']['SecurityQuestion']

const questionIdByText = new Map<string, string>()

export async function fetchSecurityQuestions(): Promise<SecurityQuestion[]> {
  const res = await fetch('/api/v1/security-questions')
  if (!res.ok) throw new Error('Failed to load security questions.')
  const data: BackendSecurityQuestion[] = await res.json()
  questionIdByText.clear()
  for (const q of data) {
    if (q.id && q.question) questionIdByText.set(q.question, q.id)
  }
  return data.map(q => q.question ?? '').filter(Boolean)
}

export async function loginUser(form: LoginForm, captchaToken: string): Promise<LoginResponse> {
  const res = await fetch('/api/v1/auth/sign-in', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mobile: form.mobile, password: form.password, captchaToken }),
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

const roleMap: Record<RegisterForm['registerAs'], NonNullable<UserRegistrationRequestDto['userRole']>> = {
  brand:    'ROLE_BRAND',
  agency:   'ROLE_AGENCY',
  mediator: 'ROLE_MEDIATOR',
  buyer:    'ROLE_BUYER',
}

export async function registerUser(form: RegisterForm, captchaToken: string): Promise<RegisterResponse> {
  const name = {
    brand:    form.brandName,
    agency:   form.agencyName,
    mediator: form.mediatorName,
    buyer:    form.buyerName,
  }[form.registerAs].trim()

  const securityQuestionList: SecurityQuestionWrapper[] = [
    { questionId: questionIdByText.get(form.securityQuestion1), answer: form.securityAnswer1.trim() },
    { questionId: questionIdByText.get(form.securityQuestion2), answer: form.securityAnswer2.trim() },
  ].filter(q => q.questionId) as SecurityQuestionWrapper[]

  const body: UserRegistrationRequestDto & { captchaToken: string } = {
    name,
    mobile: form.mobile.trim(),
    password: form.password,
    inviteCode: form.inviteCode.trim(),
    userRole: roleMap[form.registerAs],
    captchaToken,
    ...(securityQuestionList.length > 0 ? { securityQuestionList } : {}),
  }

  const res = await fetch('/api/v1/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const text = await res.text().catch(() => '')
    const message = res.status === 409
      ? 'An account with this mobile number already exists.'
      : text || 'Registration failed. Please try again.'
    return { success: false, message }
  }

  return { success: true, message: '' }
}

export async function fetchUserSecurityQuestion(_mobile: string): Promise<SecurityQuestion> {
  await new Promise(resolve => setTimeout(resolve, 400))
  // Mock: randomly return one of the two questions the user set at registration
  const userQuestions = securityQuestions.slice(0, 2)
  return userQuestions[Math.floor(Math.random() * userQuestions.length)]
}
