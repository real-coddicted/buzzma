package com.coddicted.buzzma.settings.util;

import com.coddicted.buzzma.settings.entity.Settings;

public final class SettingsUtils {

  private SettingsUtils() {}

  public static Settings getAdminSettings() {
    return Settings.builder()
        .dashboardTabEnabled(false)
        .campaignsTabEnabled(true)
        .assignmentsTabEnabled(true)
        .connectionsTabEnabled(true)
        .dealTabEnabled(true)
        .claimReviewEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(true)
        .myPaymentsTabEnabled(false)
        .userPayoutsTabEnabled(false)
        .build();
  }

  public static Settings getBrandSettings() {
    return Settings.builder()
        .dashboardTabEnabled(false)
        .campaignsTabEnabled(false)
        .assignmentsTabEnabled(false)
        .connectionsTabEnabled(true)
        .dealTabEnabled(false)
        .claimReviewEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .myPaymentsTabEnabled(false)
        .userPayoutsTabEnabled(false)
        .build();
  }

  public static Settings getAgencySettings() {
    return Settings.builder()
        .dashboardTabEnabled(false)
        .campaignsTabEnabled(true)
        .assignmentsTabEnabled(true)
        .connectionsTabEnabled(true)
        .dealTabEnabled(false)
        .claimReviewEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .myPaymentsTabEnabled(false)
        .userPayoutsTabEnabled(false)
        .build();
  }

  public static Settings getMediatorSettings() {
    return Settings.builder()
        .dashboardTabEnabled(false)
        .campaignsTabEnabled(false)
        .assignmentsTabEnabled(true)
        .connectionsTabEnabled(true)
        .dealTabEnabled(false)
        .claimReviewEnabled(true)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .myPaymentsTabEnabled(false)
        .userPayoutsTabEnabled(false)
        .build();
  }

  public static Settings getBuyerSettings() {
    return Settings.builder()
        .dashboardTabEnabled(false)
        .campaignsTabEnabled(false)
        .assignmentsTabEnabled(false)
        .connectionsTabEnabled(true)
        .dealTabEnabled(true)
        .claimReviewEnabled(false)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .myPaymentsTabEnabled(false)
        .userPayoutsTabEnabled(false)
        .build();
  }

  public static Settings getPendingConnectionSettings() {
    return Settings.builder()
        .dashboardTabEnabled(false)
        .campaignsTabEnabled(false)
        .assignmentsTabEnabled(false)
        .connectionsTabEnabled(false)
        .dealTabEnabled(false)
        .claimReviewEnabled(false)
        .ticketsTabEnabled(true)
        .feedbackTabEnabled(true)
        .settingsTabEnabled(true)
        .usersTabEnabled(false)
        .myPaymentsTabEnabled(false)
        .userPayoutsTabEnabled(false)
        .build();
  }
}
