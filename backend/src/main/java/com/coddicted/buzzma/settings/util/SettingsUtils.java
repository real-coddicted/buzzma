package com.coddicted.buzzma.settings.util;

import com.coddicted.buzzma.settings.entity.Settings;

public final class SettingsUtils {

  private SettingsUtils() {}

  public static Settings getAdminSettings() {
    return Settings.builder()
        .dashboardTabEnabled(true)
        .campaignsTabEnabled(true)
        .assignmentsTabEnabled(true)
        .connectionsTabEnabled(true)
        .dealTabEnabled(true)
        .claimReviewEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(true)
        .build();
  }

  public static Settings getBrandSettings() {
    return Settings.builder()
        .dashboardTabEnabled(true)
        .campaignsTabEnabled(true)
        .connectionsTabEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .build();
  }

  public static Settings getAgencySettings() {
    return Settings.builder()
        .dashboardTabEnabled(true)
        .campaignsTabEnabled(true)
        .connectionsTabEnabled(true)
        .claimReviewEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .build();
  }

  public static Settings getMediatorSettings() {
    return Settings.builder()
        .dashboardTabEnabled(true)
        .assignmentsTabEnabled(true)
        .connectionsTabEnabled(true)
        .claimReviewEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .build();
  }

  public static Settings getBuyerSettings() {
    return Settings.builder()
        .dealTabEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .build();
  }
}
