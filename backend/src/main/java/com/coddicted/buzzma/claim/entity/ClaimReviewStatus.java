package com.coddicted.buzzma.claim.entity;

import lombok.Getter;

@Getter
public enum ClaimReviewStatus {
  CLAIM_REVIEW_STATUS_PENDING("Pending"),
  CLAIM_REVIEW_STATUS_PROOF_REQUESTED("Proof Requested"),
  CLAIM_REVIEW_STATUS_OBJECTED("Objected"),
  CLAIM_REVIEW_STATUS_APPROVED("Approved"),
  CLAIM_REVIEW_STATUS_REJECTED("Rejected");

  private final String displayName;

  ClaimReviewStatus(final String displayName) {
    this.displayName = displayName;
  }
}
