package com.coddicted.buzzma.claim.entity;

import lombok.Getter;

@Getter
public enum ScreenshotType {
  SCREENSHOT_TYPE_ORDER("Order"),
  SCREENSHOT_TYPE_RATING("Rating"),
  SCREENSHOT_TYPE_REVIEW("Review"),
  SCREENSHOT_TYPE_RETURN("Return"),
  SCREENSHOT_TYPE_DELIVERY("Delivery"),
  SCREENSHOT_TYPE_SELLER_FEEDBACK("Seller Feedback");

  private final String displayName;

  ScreenshotType(final String displayName) {
    this.displayName = displayName;
  }
}
