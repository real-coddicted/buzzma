import type { components } from '../types/api'

const TOKEN_KEY = 'buzzma-access-token'
const USER_KEY = 'buzzma-current-user'

export type CurrentUser = components['schemas']['UserSummary']

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setAccessToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAccessToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getCurrentUser(): CurrentUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw) as CurrentUser
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export function setCurrentUser(user: CurrentUser): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearCurrentUser(): void {
  localStorage.removeItem(USER_KEY)
}

export function clearSession(): void {
  clearAccessToken()
  clearCurrentUser()
}

export async function fetchWithAuth(url: string, init: RequestInit = {}): Promise<Response> {
  const token = getAccessToken()
  const res = await fetch(url, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  })
  if (res.status === 401) {
    clearSession()
    window.dispatchEvent(new CustomEvent('auth:logout'))
    throw new Error('Session expired. Please sign in again.')
  }
  if (!res.ok) throw new Error(`${res.status}`)
  return res
}
