import { fetchWithAuth } from './client'
import type { components } from '../types/api'

export type UserSettingsDto = components['schemas']['UserSettingsDto']
export type UserSettingsFlagDto = components['schemas']['UserSettingsFlagDto']

export const FLAG_TO_DTO_KEY: Record<string, keyof UserSettingsDto> = {
  DASHBOARD_TAB_ENABLED: 'dashboardTabEnabled',
  CAMPAIGNS_TAB_ENABLED: 'campaignsTabEnabled',
  ASSIGNMENTS_TAB_ENABLED: 'assignmentsTabEnabled',
  CONNECTIONS_TAB_ENABLED: 'connectionsTabEnabled',
  DEAL_TAB_ENABLED: 'dealTabEnabled',
  CLAIM_REVIEW_ENABLED: 'claimReviewEnabled',
  TICKETS_TAB_ENABLED: 'ticketsTabEnabled',
  FEEDBACK_TAB_ENABLED: 'feedbackTabEnabled',
  SETTINGS_TAB_ENABLED: 'settingsTabEnabled',
  USERS_TAB_ENABLED: 'usersTabEnabled',
  MY_PAYMENTS_TAB_ENABLED: 'myPaymentsTabEnabled',
  USER_PAYOUTS_TAB_ENABLED: 'userPayoutsTabEnabled',
}

function flagsToDto(flags: UserSettingsFlagDto[]): UserSettingsDto {
  const dto: UserSettingsDto = {}
  for (const f of flags) {
    const key = f.flag ? FLAG_TO_DTO_KEY[f.flag] : undefined
    if (key) dto[key] = f.enabled ?? false
  }
  return dto
}

export async function fetchUserSettings(): Promise<UserSettingsDto> {
  const res = await fetchWithAuth('/api/v1/user-settings')
  const body = (await res.json()) as components['schemas']['UserSettingsFlagsResponseDto']
  return flagsToDto(body.flags ?? [])
}

export async function fetchUserSettingsById(userId: string): Promise<UserSettingsFlagDto[]> {
  const res = await fetchWithAuth(`/api/v1/user-settings/${encodeURIComponent(userId)}`)
  const body = (await res.json()) as components['schemas']['UserSettingsFlagsResponseDto']
  return body.flags ?? []
}

export async function updateUserSettingsById(userId: string, settings: UserSettingsDto): Promise<UserSettingsFlagDto[]> {
  const res = await fetchWithAuth(`/api/v1/user-settings/${encodeURIComponent(userId)}`, {
    method: 'PUT',
    body: JSON.stringify(settings),
  })
  const body = (await res.json()) as components['schemas']['UserSettingsFlagsResponseDto']
  return body.flags ?? []
}
