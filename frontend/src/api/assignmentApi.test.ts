import { describe, it, expect } from 'vitest'
import { mapSummary, mapAssignment } from './assignmentApi'
import type { components } from '../types/api'

type AssignmentSummaryResponseDto = components['schemas']['AssignmentSummaryResponseDto']
type AssignmentResponseDto = components['schemas']['AssignmentResponseDto']

function makeSummaryDto(overrides: Partial<AssignmentSummaryResponseDto> = {}): AssignmentSummaryResponseDto {
  return {
    id: 'assignment-1',
    productName: 'Test Product',
    productImageUrl: 'https://example.com/img.png',
    platform: 'PLATFORM_AMAZON',
    dealType: 'CAMPAIGN_TYPE_REVIEW',
    campaignStatus: 'CAMPAIGN_STATUS_ASSIGNED',
    originalPricePaise: 10000,
    offeredPricePaise: 8000,
    slotLimit: 5,
    ...overrides,
  }
}

function makeAssignmentDto(overrides: Partial<AssignmentResponseDto> = {}): AssignmentResponseDto {
  return {
    id: 'assignment-1',
    campaignId: 'camp-1',
    productName: 'Test Product',
    productImageUrl: 'https://example.com/img.png',
    productUrl: 'https://example.com/product',
    platform: 'PLATFORM_AMAZON',
    dealType: 'CAMPAIGN_TYPE_REVIEW',
    campaignStatus: 'CAMPAIGN_STATUS_ASSIGNED',
    originalPricePaise: 10000,
    offeredPricePaise: 8000,
    slotLimit: 5,
    ...overrides,
  }
}

describe('mapSummary', () => {
  it('carries agencyName through', () => {
    const summary = mapSummary(makeSummaryDto({ agencyName: 'Acme Agency' }))
    expect(summary.agencyName).toBe('Acme Agency')
  })

  it('leaves agencyName undefined when absent', () => {
    const summary = mapSummary(makeSummaryDto())
    expect(summary.agencyName).toBeUndefined()
  })
})

describe('mapAssignment', () => {
  it('carries agencyName through', () => {
    const assignment = mapAssignment(makeAssignmentDto({ agencyName: 'Acme Agency' }))
    expect(assignment.agencyName).toBe('Acme Agency')
  })

  it('leaves agencyName undefined when absent', () => {
    const assignment = mapAssignment(makeAssignmentDto())
    expect(assignment.agencyName).toBeUndefined()
  })
})
