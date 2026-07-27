package com.coddicted.buzzma.claim.entity;

import lombok.Getter;

@Getter
public enum ClaimStatus {
  CREATED("Created"),
  REDIRECTED("Redirected"),
  ORDERED("Ordered"),
  RATING_SUBMITTED("Rating Submitted"),
  REVIEW_SUBMITTED("Review Submitted"),
  PROOF_SUBMITTED("Proof Submitted"),
  PROOF_REJECTED("Proof Rejected"),
  UNDER_REVIEW("Under Review"),
  ADDITIONAL_PROOF_REQUESTED("Additional Proof Requested"),
  APPROVED("Approved"),
  REJECTED("Rejected"),
  REWARD_PENDING("Reward Pending"),
  COMPLETED("Completed"),
  FAILED("Failed");

  private final String displayName;

  ClaimStatus(final String displayName) {
    this.displayName = displayName;
  }
}
