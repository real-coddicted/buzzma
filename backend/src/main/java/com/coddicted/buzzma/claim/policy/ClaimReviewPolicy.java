package com.coddicted.buzzma.claim.policy;

import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;

public final class ClaimReviewPolicy {
  private ClaimReviewPolicy() {}

  public static void validateSubmitClaimReview(
      final UserRole reviewerRole, final ReviewerDecision decision) {
    if (decision == ReviewerDecision.VERIFIED && reviewerRole != UserRole.ROLE_MEDIATOR) {
      throw new BusinessRuleViolationException(
          "VERIFIED decision is only allowed for MEDIATOR role");
    }
    if (reviewerRole == UserRole.ROLE_MEDIATOR && decision != ReviewerDecision.VERIFIED) {
      throw new BusinessRuleViolationException("MEDIATOR can only submit VERIFIED decision");
    }
    if (decision == ReviewerDecision.BRAND_VERIFIED && reviewerRole != UserRole.ROLE_BRAND) {
      throw new BusinessRuleViolationException(
          "BRAND_VERIFIED decision is only allowed for BRAND role");
    }
    if (reviewerRole == UserRole.ROLE_BRAND && decision == ReviewerDecision.APPROVED) {
      throw new BusinessRuleViolationException("BRAND cannot approve a claim");
    }
  }
}
