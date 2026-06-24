import type { components } from '../types/api'
import { fetchWithAuth, setCurrentUser } from './client'

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
