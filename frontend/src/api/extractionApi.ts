import { getAccessToken, clearSession } from './client'
import type { components } from '../types/api'

export type ExtractionResult = components['schemas']['ExtractionResult']

// Export type alias for backwards compatibility
export type ExtractionResponse = ExtractionResult

export async function extractOrderDetails(file: File, requesterId?: string): Promise<ExtractionResult> {
  const formData = new FormData()
  formData.append('image', file)

  const url = new URL('/api/v1/extraction/sync', window.location.origin)
  if (requesterId) {
    url.searchParams.append('requesterId', requesterId)
  }

  const token = getAccessToken()
  const controller = new AbortController()
  const timeoutMs = 20000
  const timer = setTimeout(() => controller.abort(), timeoutMs)

  try {
    const res = await fetch(url.toString(), {
      method: 'POST',
      body: formData,
      signal: controller.signal,
      headers: {
        // Don't set Content-Type - let browser set it with correct boundary for multipart/form-data
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    })

    if (res.status === 401) {
      clearSession()
      window.dispatchEvent(new CustomEvent('auth:logout'))
      throw new Error('Session expired. Please sign in again.')
    }

    if (!res.ok) {
      let errorMessage = 'Failed to extract order details'
      try {
        const contentType = res.headers.get('content-type') ?? ''
        if (contentType.includes('application/json')) {
          const body = await res.clone().json() as Record<string, unknown>
          if (typeof body['message'] === 'string') {
            errorMessage = body['message']
          }
        } else {
          const text = (await res.clone().text()).trim()
          if (text && text.length < 200) {
            errorMessage = text
          }
        }
      } catch (parseErr) {
        console.error('Failed to parse error response:', parseErr)
      }
      throw new Error(errorMessage)
    }

    return (await res.json()) as ExtractionResult
  } catch (err) {
    if ((err as { name?: string })?.name === 'AbortError') {
      throw new Error(`Request timed out after ${timeoutMs / 1000}s. Please try again.`)
    }
    throw err
  } finally {
    clearTimeout(timer)
  }
}
