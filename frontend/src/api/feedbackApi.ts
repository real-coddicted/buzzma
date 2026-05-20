import type { operations } from '../types/api'
import { fetchWithAuth } from './client'

export type FeedbackRequest = operations['create_3']['requestBody']['content']['application/json']
export type FeedbackResponse = operations['create_3']['responses'][201]['content']['*/*']

export async function submitFeedback(body: FeedbackRequest): Promise<FeedbackResponse> {
  const res = await fetchWithAuth('/api/v1/feedback', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return res.json() as Promise<FeedbackResponse>
}


