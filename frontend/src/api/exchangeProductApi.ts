import { fetchWithAuth } from './client'
import type { components } from '../types/api'

export type ExchangeProductResponseDto = components['schemas']['ExchangeProductResponseDto']
type ExchangeProductRequestDto = components['schemas']['ExchangeProductRequestDto']

const API_BASE = '/api/v1'

/** GET /agency/exchange-products — the agency's saved exchange-product master list. */
export async function fetchExchangeProducts(): Promise<ExchangeProductResponseDto[]> {
  const res = await fetchWithAuth(`${API_BASE}/agency/exchange-products`)
  return (await res.json()) as ExchangeProductResponseDto[]
}

/** POST /agency/exchange-products — adds a product name to agency_exchange_products. */
export async function createExchangeProduct(name: string): Promise<ExchangeProductResponseDto> {
  const body: ExchangeProductRequestDto = { name }
  const res = await fetchWithAuth(`${API_BASE}/agency/exchange-products`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return (await res.json()) as ExchangeProductResponseDto
}
