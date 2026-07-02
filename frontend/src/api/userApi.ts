import type { components } from '../types/api'
import { fetchWithAuth, setCurrentUser } from './client'
import type { UserBankingDto, UserActivityDto } from '../types/ProfileTypes'

export type UserSummaryDto = components['schemas']['UserSummaryDto']

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

export async function fetchUserBanking(userId: string): Promise<UserBankingDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}/banking`)
  return res.json() as Promise<UserBankingDto>
}

export async function fetchUserActivity(userId: string): Promise<UserActivityDto> {
  const res = await fetchWithAuth(`/api/v1/users/${userId}/activity`)
  return res.json() as Promise<UserActivityDto>
}
