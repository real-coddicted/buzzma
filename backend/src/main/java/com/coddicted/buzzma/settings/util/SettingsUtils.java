package com.coddicted.buzzma.settings.util;

import com.coddicted.buzzma.settings.entity.Settings;

public final class SettingsUtils {

  private SettingsUtils() {}

  public static Settings getAdminSettings() {
    return Settings.builder()
        .isDashboardTabEnabled(true)
        .isCampaignsTabEnabled(true)
        .isAssignmentsTabEnabled(true)
        .isConnectionsTabEnabled(true)
        .isDealTabEnabled(true)
        .isTicketsTabEnabled(true)
        .isFeedbackTabEnabled(true)
        .isSettingsTabEnabled(true)
        .build();
  }

  public static Settings getBrandSettings() {
    return Settings.builder()
        .isDashboardTabEnabled(true)
        .isCampaignsTabEnabled(true)
        .isConnectionsTabEnabled(true)
        .isTicketsTabEnabled(true)
        .isFeedbackTabEnabled(true)
        .isSettingsTabEnabled(true)
        .build();
  }

  public static Settings getAgencySettings() {
    return Settings.builder()
        .isDashboardTabEnabled(true)
        .isCampaignsTabEnabled(true)
        .isConnectionsTabEnabled(true)
        .isTicketsTabEnabled(true)
        .isFeedbackTabEnabled(true)
        .isSettingsTabEnabled(true)
        .build();
  }

  public static Settings getMediatorSettings() {
    return Settings.builder()
        .isDashboardTabEnabled(true)
        .isAssignmentsTabEnabled(true)
        .isConnectionsTabEnabled(true)
        .isTicketsTabEnabled(true)
        .isFeedbackTabEnabled(true)
        .isSettingsTabEnabled(true)
        .build();
  }

  public static Settings getBuyerSettings() {
    return Settings.builder()
        .isDealTabEnabled(true)
        .isTicketsTabEnabled(true)
        .isFeedbackTabEnabled(true)
        .isSettingsTabEnabled(true)
        .build();
  }
}
