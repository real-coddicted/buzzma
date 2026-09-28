package com.coddicted.buzzma.claim.entity;

import lombok.Getter;

@Getter
public enum ClaimStatus {
  ORDERED("Ordered"),
  DOWNLOADED_AND_INSTALLED("Downloaded & Installed"),
  DELIVERY_PROOF_SUBMITTED("Delivery Proof Submitted"),
  RATING_SUBMITTED("Rating Submitted"),
  REVIEW_SUBMITTED("Review Submitted"),
  SELLER_FEEDBACK_SUBMITTED("Seller Feedback Submitted"),
  VIEW_PROOF_SUBMITTED("View Proof Submitted"),
  LIKE_PROOF_SUBMITTED("Like Proof Submitted"),
  FOLLOW_PROOF_SUBMITTED("Follow Proof Submitted"),
  COMMENT_PROOF_SUBMITTED("Comment Proof Submitted"),
  SUBSCRIBE_PROOF_SUBMITTED("Subscribe Proof Submitted"),
  PROOF_SUBMITTED("Proof Submitted"),
  PROOF_REJECTED("Proof Rejected"),
  UNDER_REVIEW("Under Review"),
  ADDITIONAL_PROOF_REQUESTED("Additional Proof Requested"),
  APPROVED("Approved"),
  READY_FOR_ACCOUNTING("Ready for Accounting"),
  REJECTED("Rejected"),
  REWARD_PENDING("Reward Pending"),
  COMPLETED("Completed"),
  FAILED("Failed");

  private final String displayName;

  ClaimStatus(final String displayName) {
    this.displayName = displayName;
  }
}
