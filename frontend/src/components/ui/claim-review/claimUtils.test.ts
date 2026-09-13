import { describe, it, expect } from 'vitest'
import { isClaimLocked, canResetClaim, isReviewSubmitDisabled } from './claimUtils'
import type { ClaimStatus } from '../../../types'

describe('isClaimLocked', () => {
  it('is locked once a claim has been decided or moved to accounting or beyond', () => {
    const locked: ClaimStatus[] = [
      'APPROVED',
      'REJECTED',
      'READY_FOR_ACCOUNTING',
      'REWARD_PENDING',
      'COMPLETED',
      'FAILED',
    ]
    for (const status of locked) {
      expect(isClaimLocked(status)).toBe(true)
    }
  })

  it('is not locked while still in review', () => {
    const notLocked: ClaimStatus[] = ['ORDERED', 'PROOF_SUBMITTED', 'PROOF_REJECTED', 'UNDER_REVIEW']
    for (const status of notLocked) {
      expect(isClaimLocked(status)).toBe(false)
    }
  })

  it('is not locked when status is undefined', () => {
    expect(isClaimLocked(undefined)).toBe(false)
  })
})

describe('canResetClaim', () => {
  it('allows agency to reset an approved claim', () => {
    expect(canResetClaim('ROLE_AGENCY', 'APPROVED')).toBe(true)
  })

  it('allows brand to reset an approved claim', () => {
    expect(canResetClaim('ROLE_BRAND', 'APPROVED')).toBe(true)
  })

  it('disallows resetting a claim that is not approved', () => {
    expect(canResetClaim('ROLE_AGENCY', 'UNDER_REVIEW')).toBe(false)
    expect(canResetClaim('ROLE_BRAND', 'REJECTED')).toBe(false)
  })

  it('disallows roles other than agency/brand from resetting', () => {
    expect(canResetClaim('ROLE_MEDIATOR', 'APPROVED')).toBe(false)
    expect(canResetClaim(undefined, 'APPROVED')).toBe(false)
  })
})

describe('isReviewSubmitDisabled', () => {
  it('is disabled when the claim is locked, regardless of the selected status', () => {
    expect(
      isReviewSubmitDisabled(
        true,
        'SCREENSHOT_VERIFICATION_STATUS_VERIFIED',
        'SCREENSHOT_VERIFICATION_STATUS_PENDING',
      ),
    ).toBe(true)
  })

  it('is disabled when the selected status matches the server value (no-op guard)', () => {
    expect(
      isReviewSubmitDisabled(
        false,
        'SCREENSHOT_VERIFICATION_STATUS_PENDING',
        'SCREENSHOT_VERIFICATION_STATUS_PENDING',
      ),
    ).toBe(true)
  })

  it('is enabled when unlocked and the selected status differs from the server value', () => {
    expect(
      isReviewSubmitDisabled(
        false,
        'SCREENSHOT_VERIFICATION_STATUS_VERIFIED',
        'SCREENSHOT_VERIFICATION_STATUS_PENDING',
      ),
    ).toBe(false)
  })
})
