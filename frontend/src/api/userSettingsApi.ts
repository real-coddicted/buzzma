import { fetchWithAuth } from './client'
import type { components } from '../types/api'

export type UserSettingsDto = components['schemas']['UserSettingsDto']

export async function fetchUserSettings(): Promise<UserSettingsDto> {
  const res = await fetchWithAuth('/api/v1/user-settings')
  return (await res.json()) as UserSettingsDto
}

export async function fetchUserSettingsById(userId: string): Promise<UserSettingsDto> {
  const res = await fetchWithAuth(`/api/v1/user-settings/${encodeURIComponent(userId)}`)
  return (await res.json()) as UserSettingsDto
}

export async function updateUserSettingsById(userId: string, settings: UserSettingsDto): Promise<UserSettingsDto> {
  const res = await fetchWithAuth(`/api/v1/user-settings/${encodeURIComponent(userId)}`, {
    method: 'PUT',
    body: JSON.stringify(settings),
  })
  return (await res.json()) as UserSettingsDto
}
