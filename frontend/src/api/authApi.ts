import type {
  PasswordResetChallenge,
  PasswordResetResult,
  SecurityQuestion,
  UserSecurityQuestionResult,
} from '../types/ForgotPasswordTypes'
import type { RegisterForm, RegisterResponse } from '../types/RegisterTypes'
import type { LoginForm, LoginResponse } from '../types/LoginTypes'
import type { components } from '../types/api'
import { clearCurrentUser, fetchWithAuth, scheduleProactiveRefresh, setAccessToken, setCurrentUser } from './client'

type SignInResponse = components['schemas']['UserSignInResponseDto']
type UserRegistrationRequestDto = components['schemas']['UserRegistrationRequestDto']
type PasswordUpdateRequestDto = components['schemas']['PasswordUpdateRequestDto']
type SecurityQuestionWrapper = components['schemas']['SecurityQuestionWrapper']
type BackendSecurityQuestion = components['schemas']['SecurityQuestion']
type SecurityQuestionResponseDto = components['schemas']['SecurityQuestionResponseDto']
type PasswordResetRequestDto = components['schemas']['PasswordResetRequestDto']

const questionIdByText = new Map<string, string>()
const questionTextById = new Map<string, string>()

export async function fetchSecurityQuestions(): Promise<SecurityQuestion[]> {
  const res = await fetch('/api/v1/security-questions')
  if (!res.ok) throw new Error('Failed to load security questions.')
  const data: BackendSecurityQuestion[] = await res.json()
  questionIdByText.clear()
  questionTextById.clear()
  for (const q of data) {
    if (q.id && q.question) {
      questionIdByText.set(q.question, q.id)
      questionTextById.set(q.id, q.question)
    }
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
    const data = await res.json().catch(() => null)
    const message = data?.message ?? 'Something went wrong. Please try again.'
    return { success: false, message }
  }
  const data: SignInResponse = await res.json()
  if (data.tokens?.accessToken) {
    setAccessToken(data.tokens.accessToken)
    scheduleProactiveRefresh()
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
    email: form.email.trim(),
    password: form.password,
    inviteCode: form.inviteCode.trim(),
    userRole: roleMap[form.registerAs],
    captchaToken,
    termsAccepted: form.termsAccepted,
    ...(securityQuestionList.length > 0 ? { securityQuestionList } : {}),
  }

  const res = await fetch('/api/v1/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (!res.ok) {
    const data = await res.json().catch(() => null)
    const message = data?.message ?? 'Registration failed. Please try again.'
    return { success: false, message }
  }

  return { success: true, message: '' }
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  const body: PasswordUpdateRequestDto = { currentPassword, newPassword }
  await fetchWithAuth('/api/v1/auth/password-update', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function signOut(): Promise<void> {
  await fetch('/api/v1/auth/sign-out', {
    method: 'POST',
    credentials: 'include',
  })
}

export async function fetchUserSecurityQuestion(mobile: string): Promise<UserSecurityQuestionResult> {
  const res = await fetch('/api/v1/auth/forgot-password/lookup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mobile }),
  })
  if (res.status === 404) {
    return { success: false, message: 'No account found for this mobile number.' }
  }
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    return { success: false, message: data?.message ?? 'Something went wrong. Please try again.' }
  }
  const data: SecurityQuestionResponseDto[] = await res.json()
  const questionId = data.find(q => q.questionId)?.questionId
  if (!questionId) {
    return {
      success: false,
      message: 'No security question is set for this account. Please contact support.',
    }
  }
  if (!questionTextById.has(questionId)) await fetchSecurityQuestions()
  const question = questionTextById.get(questionId)
  if (!question) return { success: false, message: 'Something went wrong. Please try again.' }
  return { success: true, questionId, question }
}

export async function resetPassword(
  challenge: PasswordResetChallenge,
  newPassword: string,
): Promise<PasswordResetResult> {
  const body: PasswordResetRequestDto = { ...challenge, newPassword }
  const res = await fetch('/api/v1/auth/password-reset', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    return { success: false, message: data?.message ?? 'Password reset failed. Please try again.' }
  }
  return { success: true, message: '' }
}
