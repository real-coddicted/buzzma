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

const DEFAULT_TIMEOUT_MS = 5000

export async function fetchWithAuth(
  url: string,
  init: RequestInit = {},
  timeoutMs: number = DEFAULT_TIMEOUT_MS,
): Promise<Response> {
  const token = getAccessToken()
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)

  let res: Response
  try {
    res = await fetch(url, {
      ...init,
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...init.headers,
      },
    })
  } catch (err) {
    if ((err as { name?: string })?.name === 'AbortError') {
      throw new Error(`Request timed out after ${timeoutMs / 1000}s. Please try again.`)
    }
    throw err
  } finally {
    clearTimeout(timer)
  }

  if (res.status === 401) {
    clearSession()
    window.dispatchEvent(new CustomEvent('auth:logout'))
    throw new Error('Session expired. Please sign in again.')
  }

  if (!res.ok) {
    // Try to extract a message from the response body first
    let bodyMessage: string | undefined
    try {
      const contentType = res.headers.get('content-type') ?? ''
      if (contentType.includes('application/json')) {
        const body = await res.clone().json() as Record<string, unknown>

        // Try to surface field-level validation errors first
        const validationErrors = body['errors'] ?? body['validationErrors'] ?? body['fieldErrors']
        if (Array.isArray(validationErrors) && validationErrors.length > 0) {
          const lines = (validationErrors as Record<string, unknown>[])
            .map(e => {
              const field   = typeof e['field']   === 'string' ? e['field']   : null
              const message = typeof e['message'] === 'string' ? e['message'] : null
              if (field && message) return `${humanizeField(field)}: ${humanizeCode(message)}`
              if (message) return humanizeCode(message)
              return null
            })
            .filter(Boolean)
          if (lines.length > 0) {
            bodyMessage = lines.join('\n')
          }
        }

        // Fall back to the top-level message / error string
        if (!bodyMessage) {
          const raw =
            typeof body['message'] === 'string' ? body['message'] :
            typeof body['error']   === 'string' ? body['error']   :
            undefined
          if (raw) bodyMessage = humanizeCode(raw)
        }
      } else {
        const text = (await res.clone().text()).trim()
        if (text && text.length < 200) bodyMessage = humanizeCode(text)
      }
    } catch (parseErr) {
      console.error('Failed to parse error response body:', parseErr)
    }

    const fallback: Record<number, string> = {
      400: 'Bad request. Please check your input and try again.',
      403: 'You don\'t have permission to perform this action.',
      404: 'The requested resource was not found.',
      409: 'A conflict occurred. The resource may already exist.',
      422: 'Unprocessable request. Please check your input.',
      429: 'Too many requests. Please wait a moment and try again.',
      500: 'A server error occurred. Please try again later.',
      502: 'Service unavailable. Please try again later.',
      503: 'Service unavailable. Please try again later.',
    }

    throw new Error(bodyMessage ?? fallback[res.status] ?? `Request failed (${res.status}).`)
  }

  return res
}

/** Map known ALL_CAPS / SNAKE_CASE backend error codes to readable sentences. */
function humanizeCode(raw: string): string {
  const known: Record<string, string> = {
    VALIDATION_FAILED:            'Validation failed. Please check your input.',
    INVALID_CREDENTIALS:          'Invalid credentials. Please try again.',
    USER_NOT_FOUND:               'User not found.',
    CAMPAIGN_NOT_FOUND:           'Campaign not found.',
    ASSIGNMENT_NOT_FOUND:         'Assignment not found.',
    DEAL_NOT_FOUND:               'Deal not found.',
    TICKET_NOT_FOUND:             'Ticket not found.',
    CONNECTION_NOT_FOUND:         'Connection not found.',
    ALREADY_EXISTS:               'A record with these details already exists.',
    DUPLICATE_ENTRY:              'A duplicate entry was detected.',
    FORBIDDEN:                    'You don\'t have permission to perform this action.',
    UNAUTHORIZED:                 'You are not authorised. Please sign in again.',
    INVITE_CODE_INVALID:          'The invite code is invalid or has expired.',
    INVITE_CODE_EXPIRED:          'The invite code has expired.',
    QUOTA_EXCEEDED:               'Quota exceeded. Please try again later.',
    SLOT_UNAVAILABLE:             'No slots are available for this campaign.',
    INTERNAL_SERVER_ERROR:        'A server error occurred. Please try again later.',
  }
  if (known[raw]) return known[raw]

  // Generic transform: "SOME_ERROR_CODE" → "Some error code."
  if (/^[A-Z][A-Z0-9_]+$/.test(raw)) {
    const sentence = raw.replace(/_/g, ' ').toLowerCase()
    return sentence.charAt(0).toUpperCase() + sentence.slice(1) + '.'
  }

  return raw
}

/** Convert a camelCase / snake_case field name to a readable label. */
function humanizeField(field: string): string {
  return field
    .replace(/([A-Z])/g, ' $1')   // camelCase → words
    .replace(/_/g, ' ')            // snake_case → words
    .trim()
    .replace(/^\w/, c => c.toUpperCase())
}

