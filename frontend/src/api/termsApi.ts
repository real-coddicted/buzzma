import { fetchWithAuth } from './client'
import type { components } from '../types/api'

export type TermsDto = components['schemas']['TermsDto']
export type TermsAcceptanceStatusDto = components['schemas']['TermsAcceptanceStatusDto']

export async function fetchTerms(): Promise<TermsDto> {
  const res = await fetch('/api/v1/terms')
  if (!res.ok) throw new Error('Failed to load terms and conditions.')
  return (await res.json()) as TermsDto
}

export async function fetchTermsAcceptanceStatus(): Promise<TermsAcceptanceStatusDto> {
  const res = await fetchWithAuth('/api/v1/terms/acceptance-status')
  return (await res.json()) as TermsAcceptanceStatusDto
}

export async function acceptTerms(): Promise<void> {
  await fetchWithAuth('/api/v1/terms/accept', { method: 'POST' })
}