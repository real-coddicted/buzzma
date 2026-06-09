import { fetchWithAuth } from './client'
import type { components } from '../types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

export async function fetchUserSettings(): Promise<UserSettingsDto> {
  const res = await fetchWithAuth('/api/v1/user-settings')
  return (await res.json()) as UserSettingsDto
}
