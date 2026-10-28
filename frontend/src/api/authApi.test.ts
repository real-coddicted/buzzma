import { describe, it, expect, vi, afterEach } from 'vitest'
import { fetchUserSecurityQuestion, resetPassword } from './authApi'

const QUESTION_ID = '55555555-5555-5555-5555-555555555555'

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('fetchUserSecurityQuestion', () => {
  it('returns the first question with its text resolved from the questions list', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse(200, [{ questionId: QUESTION_ID }, { questionId: 'other' }]))
      .mockResolvedValueOnce(jsonResponse(200, [{ id: QUESTION_ID, question: 'First pet?' }]))
    vi.stubGlobal('fetch', fetchMock)

    const result = await fetchUserSecurityQuestion('9876543210')

    expect(result).toEqual({ success: true, questionId: QUESTION_ID, question: 'First pet?' })
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/auth/forgot-password/lookup', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ mobile: '9876543210' }),
    }))
  })

  it('reports an unknown mobile number', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValueOnce(jsonResponse(404, { message: 'user not found' })))

    const result = await fetchUserSecurityQuestion('9876543210')

    expect(result).toEqual({ success: false, message: 'No account found for this mobile number.' })
  })

  it('reports an account without a security question', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValueOnce(jsonResponse(200, [])))

    const result = await fetchUserSecurityQuestion('9876543210')

    expect(result.success).toBe(false)
  })
})

describe('resetPassword', () => {
  const challenge = { mobile: '9876543210', questionId: QUESTION_ID, answer: 'rex' }

  it('sends the challenge with the new password', async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce(jsonResponse(200, true))
    vi.stubGlobal('fetch', fetchMock)

    const result = await resetPassword(challenge, 'NewPass@123')

    expect(result).toEqual({ success: true, message: '' })
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/auth/password-reset', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ ...challenge, newPassword: 'NewPass@123' }),
    }))
  })

  it('surfaces the backend message when the answer is wrong', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValueOnce(jsonResponse(401, { message: 'Incorrect security answer' })))

    const result = await resetPassword(challenge, 'NewPass@123')

    expect(result).toEqual({ success: false, message: 'Incorrect security answer' })
  })
})
