package com.coddicted.buzzma.claim.entity;

import lombok.Getter;

@Getter
public enum ScreenshotType {
  SCREENSHOT_TYPE_ORDER("Order"),
  SCREENSHOT_TYPE_RATING("Rating"),
  SCREENSHOT_TYPE_REVIEW("Review"),
  SCREENSHOT_TYPE_RETURN("Return");

  private final String displayName;

  ScreenshotType(final String displayName) {
    this.displayName = displayName;
  }
}
