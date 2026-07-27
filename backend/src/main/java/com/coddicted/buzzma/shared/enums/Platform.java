package com.coddicted.buzzma.shared.enums;

import lombok.Getter;

// this should come from database, but for now we are hardcoding it as enum
// the idea is that an admin should be enable/disable the platforms from the admin panel, and the
// campaign can be created for the enabled platforms only.
@Getter
public enum Platform {
  PLATFORM_AMAZON("Amazon"),
  PLATFORM_FLIPKART("Flipkart"),
  PLATFORM_NYKAA("Nykaa"),
  PLATFORM_MYNTRA("Myntra");

  private final String displayName;

  Platform(final String displayName) {
    this.displayName = displayName;
  }
}
