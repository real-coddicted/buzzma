package com.coddicted.buzzma.shared.enums;

import lombok.Getter;

/** Platform supported (or to be supported) by Buzzmah. */
@Getter
public enum Platform {
  PLATFORM_AMAZON("Amazon"),
  PLATFORM_FLIPKART("Flipkart"),
  PLATFORM_NYKAA("Nykaa"),
  PLATFORM_MYNTRA("Myntra"),
  PLATFORM_MEESHO("Meesho"),
  PLATFORM_BLINKIT("Blinkit"),
  PLATFORM_ZEPTO("Zepto"),
  PLATFORM_APPLE_APP_STORE("Apple App Store"),
  PLATFORM_GOOGLE_PLAY_STORE("Google Play Store"),
  PLATFORM_YOUTUBE("YouTube"),
  PLATFORM_INSTAGRAM("Instagram"),
  PLATFORM_GOOGLE_REVIEWS("Google Reviews"),
  PLATFORM_OTHER("Other");

  private final String displayName;

  Platform(final String displayName) {
    this.displayName = displayName;
  }
}
