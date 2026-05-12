import type { components } from '../types/api'
import { fetchWithAuth } from './client'

type FeedbackRequest  = components['schemas']['FeedbackRequestDto']
type FeedbackResponse = components['schemas']['FeedbackResponseDto']

export async function submitFeedback(body: FeedbackRequest): Promise<FeedbackResponse> {
  const res = await fetchWithAuth('/api/v1/feedback', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return res.json() as Promise<FeedbackResponse>
}
