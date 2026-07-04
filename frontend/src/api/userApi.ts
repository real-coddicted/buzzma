import type { components } from '../types/api'
import { fetchWithAuth, setCurrentUser } from './client'
import type { UserActivityDto } from '../types/ProfileTypes'

export type UserSummaryDto      = components['schemas']['UserSummaryDto']
export type UserBankingDetailDto = components['schemas']['UserBankingDetailDto']

export async function fetchCurrentUser(): Promise<UserSummaryDto> {
  const res = await fetchWithAuth('/api/v1/users/me')
  const user: UserSummaryDto = await res.json()
  setCurrentUser(user)
  return user
}

export async function searchUserByMobile(mobile: string): Promise<UserSummaryDto> {
  const res = await fetchWithAuth(`/api/v1/users/search?mobile=${encodeURIComponent(mobile)}`)
  return res.json() as Promise<UserSummaryDto>
}

export async function fetchUserById(userId: string): Promise<UserSummaryDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}`)
  return res.json() as Promise<UserSummaryDto>
}

export async function fetchUserBanking(userId: string): Promise<UserBankingDetailDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}/banking`)
  return res.json() as Promise<UserBankingDetailDto>
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
