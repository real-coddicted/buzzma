package com.coddicted.buzzma.claim.entity;

import lombok.Getter;

@Getter
public enum ScreenshotVerificationStatus {
  SCREENSHOT_VERIFICATION_STATUS_PENDING("Pending"),
  SCREENSHOT_VERIFICATION_STATUS_VERIFIED("Verified"),
  SCREENSHOT_VERIFICATION_STATUS_REJECTED("Rejected");

  private final String displayName;

  ScreenshotVerificationStatus(final String displayName) {
    this.displayName = displayName;
  }
}
