package com.coddicted.buzzma.claim.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class ClaimReviewPolicyTest {

  @Test
  void testVerifiedByMediatorIsAllowed() {
    assertDoesNotThrow(
        () ->
            ClaimReviewPolicy.validateSubmitClaimReview(
                UserRole.ROLE_MEDIATOR, ReviewerDecision.VERIFIED));
  }

  @Test
  void testVerifiedByNonMediatorThrows() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                ClaimReviewPolicy.validateSubmitClaimReview(
                    UserRole.ROLE_AGENCY, ReviewerDecision.VERIFIED));
    assertEquals("VERIFIED decision is only allowed for MEDIATOR role", ex.getMessage());
  }

  @Test
  void testMediatorSubmittingNonVerifiedDecisionThrows() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                ClaimReviewPolicy.validateSubmitClaimReview(
                    UserRole.ROLE_MEDIATOR, ReviewerDecision.APPROVED));
    assertEquals("MEDIATOR can only submit VERIFIED decision", ex.getMessage());
  }

  @Test
  void testBrandVerifiedByBrandIsAllowed() {
    assertDoesNotThrow(
        () ->
            ClaimReviewPolicy.validateSubmitClaimReview(
                UserRole.ROLE_BRAND, ReviewerDecision.BRAND_VERIFIED));
  }

  @Test
  void testBrandVerifiedByNonBrandThrows() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                ClaimReviewPolicy.validateSubmitClaimReview(
                    UserRole.ROLE_AGENCY, ReviewerDecision.BRAND_VERIFIED));
    assertEquals("BRAND_VERIFIED decision is only allowed for BRAND role", ex.getMessage());
  }

  @Test
  void testApprovedByBrandThrows() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                ClaimReviewPolicy.validateSubmitClaimReview(
                    UserRole.ROLE_BRAND, ReviewerDecision.APPROVED));
    assertEquals("BRAND cannot approve a claim", ex.getMessage());
  }

  @Test
  void testApprovedByAgencyIsAllowed() {
    assertDoesNotThrow(
        () ->
            ClaimReviewPolicy.validateSubmitClaimReview(
                UserRole.ROLE_AGENCY, ReviewerDecision.APPROVED));
  }

  @Test
  void testRejectedByAgencyIsAllowed() {
    assertDoesNotThrow(
        () ->
            ClaimReviewPolicy.validateSubmitClaimReview(
                UserRole.ROLE_AGENCY, ReviewerDecision.REJECTED));
  }
}
