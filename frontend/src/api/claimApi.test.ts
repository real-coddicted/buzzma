import { describe, it, expect } from 'vitest'
import { mapPagedClaimsResponse } from './claimApi'
import type { components } from '../types/api'

type PagedClaimsResponseDto = components['schemas']['PagedClaimsResponseDto']
type ClaimResponseDto = components['schemas']['ClaimResponseDto']

function makeClaimDto(overrides: Partial<ClaimResponseDto> = {}): ClaimResponseDto {
  return {
    id: 'claim-1',
    code: 'CLM1-A2B3',
    status: 'ORDERED',
    ...overrides,
  }
}

describe('mapPagedClaimsResponse', () => {
  it('converts the backend 0-based page to a 1-based page', () => {
    const dto: PagedClaimsResponseDto = { items: [], total: 0, page: 0, totalPages: 1 }
    expect(mapPagedClaimsResponse(dto).page).toBe(1)
  })

  it('carries items and total through unchanged', () => {
    const claim = makeClaimDto()
    const dto: PagedClaimsResponseDto = { items: [claim], total: 1, page: 0, totalPages: 1 }
    const result = mapPagedClaimsResponse(dto)
    expect(result.items).toEqual([claim])
    expect(result.total).toBe(1)
  })

  it('falls back to empty items and zero total when absent', () => {
    const result = mapPagedClaimsResponse({})
    expect(result.items).toEqual([])
    expect(result.total).toBe(0)
    expect(result.page).toBe(1)
    expect(result.totalPages).toBe(1)
  })
})
