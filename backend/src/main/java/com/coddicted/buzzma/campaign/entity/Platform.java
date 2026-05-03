package com.coddicted.buzzma.campaign.entity;

// this should come from database, but for now we are hardcoding it as enum
// the idea is that an admin should be enable/disable the platforms from the admin panel, and the
// campaign can be created for the enabled platforms only.
public enum Platform {
  PLATFORM_AMAZON,
  PLATFORM_FLIPKART,
  PLATFORM_NYKAA,
  PLATFORM_MYNTRA
}
