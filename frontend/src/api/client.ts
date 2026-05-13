const TOKEN_KEY = 'buzzma-access-token'

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setAccessToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAccessToken(): void {
  localStorage.removeItem(TOKEN_KEY)
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
    clearAccessToken()
    window.dispatchEvent(new CustomEvent('auth:logout'))
    throw new Error('Session expired. Please sign in again.')
  }
  if (!res.ok) throw new Error(`${res.status}`)
  return res
}
