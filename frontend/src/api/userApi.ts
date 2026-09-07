import type { components } from '../types/api'
import { fetchWithAuth, setCurrentUser } from './client'
import type { UserActivityDto } from '../types/ProfileTypes'

export type UserSummaryDto      = components['schemas']['UserSummaryDto']
export type UserBankingDetailDto = components['schemas']['UserBankingDetailDto']
export type PagedUsersResponseDto = components['schemas']['PagedUsersResponseDto']

export async function fetchCurrentUser(): Promise<UserSummaryDto> {
  const res = await fetchWithAuth('/api/v1/users/me')
  const user: UserSummaryDto = await res.json()
  setCurrentUser(user)
  return user
}

export async function updateUserProfile(email: string): Promise<UserSummaryDto> {
  const res = await fetchWithAuth('/api/v1/users/me', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
  const user: UserSummaryDto = await res.json()
  setCurrentUser(user)
  return user
}

export async function sendEmailOtp(): Promise<void> {
  await fetchWithAuth('/api/v1/users/me/email/otp/send', { method: 'POST' })
}

export async function verifyEmailOtp(code: string): Promise<UserSummaryDto> {
  const res = await fetchWithAuth('/api/v1/users/me/email/otp/verify', {
    method: 'POST',
    body: JSON.stringify({ code }),
  })
  const user: UserSummaryDto = await res.json()
  setCurrentUser(user)
  return user
}

export async function searchUsers(term: string, page = 0, size = 20): Promise<PagedUsersResponseDto> {
  const res = await fetchWithAuth(
    `/api/v1/users/search?q=${encodeURIComponent(term)}&page=${page}&size=${size}`,
  )
  return res.json() as Promise<PagedUsersResponseDto>
}

export async function fetchUserById(userId: string): Promise<UserSummaryDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}`)
  return res.json() as Promise<UserSummaryDto>
}

export async function fetchUserBanking(userId: string): Promise<UserBankingDetailDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}/banking`)
  return res.json() as Promise<UserBankingDetailDto>
}

export interface UserBriefDto {
  id: string
  name: string
  role: string
  upiId: string | null
}

export async function fetchUsersByIds(ids: string[]): Promise<UserBriefDto[]> {
  if (ids.length === 0) return []
  const res = await fetchWithAuth('/api/v1/users/batch', {
    method: 'POST',
    body: JSON.stringify({ ids }),
  })
  return res.json() as Promise<UserBriefDto[]>
}

export async function upsertUserBanking(userId: string, data: UserBankingDetailDto): Promise<UserBankingDetailDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}/banking`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
  return res.json() as Promise<UserBankingDetailDto>
}

export async function fetchUserActivity(userId: string): Promise<UserActivityDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}/activity`)
  return res.json() as Promise<UserActivityDto>
}
