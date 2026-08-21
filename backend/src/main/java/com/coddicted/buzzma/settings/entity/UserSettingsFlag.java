package com.coddicted.buzzma.settings.entity;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum UserSettingsFlag {
  DASHBOARD_TAB_ENABLED(
      "Dashboard", Settings::getDashboardTabEnabled, Settings.SettingsBuilder::dashboardTabEnabled),
  CAMPAIGNS_TAB_ENABLED(
      "Campaigns", Settings::getCampaignsTabEnabled, Settings.SettingsBuilder::campaignsTabEnabled),
  ASSIGNMENTS_TAB_ENABLED(
      "Assignments",
      Settings::getAssignmentsTabEnabled,
      Settings.SettingsBuilder::assignmentsTabEnabled),
  CONNECTIONS_TAB_ENABLED(
      "Connections",
      Settings::getConnectionsTabEnabled,
      Settings.SettingsBuilder::connectionsTabEnabled),
  DEAL_TAB_ENABLED("Deals", Settings::getDealTabEnabled, Settings.SettingsBuilder::dealTabEnabled),
  MY_CLAIMS_TAB_ENABLED(
      "My Claims", Settings::getMyClaimsTabEnabled, Settings.SettingsBuilder::myClaimsTabEnabled),
  CLAIM_REVIEW_ENABLED(
      "Claim Review",
      Settings::getClaimReviewEnabled,
      Settings.SettingsBuilder::claimReviewEnabled),
  TICKETS_TAB_ENABLED(
      "Tickets", Settings::getTicketsTabEnabled, Settings.SettingsBuilder::ticketsTabEnabled),
  FEEDBACK_TAB_ENABLED(
      "Feedback", Settings::getFeedbackTabEnabled, Settings.SettingsBuilder::feedbackTabEnabled),
  SETTINGS_TAB_ENABLED(
      "Settings", Settings::getSettingsTabEnabled, Settings.SettingsBuilder::settingsTabEnabled),
  USERS_TAB_ENABLED(
      "Users", Settings::getUsersTabEnabled, Settings.SettingsBuilder::usersTabEnabled),
  MY_PAYMENTS_TAB_ENABLED(
      "My Payments",
      Settings::getMyPaymentsTabEnabled,
      Settings.SettingsBuilder::myPaymentsTabEnabled),
  USER_PAYOUTS_TAB_ENABLED(
      "User Payouts",
      Settings::getUserPayoutsTabEnabled,
      Settings.SettingsBuilder::userPayoutsTabEnabled);

  private final String displayName;
  private final Function<Settings, Boolean> valueExtractor;
  private final BiFunction<Settings.SettingsBuilder, Boolean, Settings.SettingsBuilder> setter;

  UserSettingsFlag(
      final String displayName,
      final Function<Settings, Boolean> valueExtractor,
      final BiFunction<Settings.SettingsBuilder, Boolean, Settings.SettingsBuilder> setter) {
    this.displayName = displayName;
    this.valueExtractor = valueExtractor;
    this.setter = setter;
  }

  /** Raw, possibly-null value as stored, without any role-default resolution. */
  public Boolean getValue(final Settings settings) {
    return this.valueExtractor.apply(settings);
  }

  public boolean isEnabled(final Settings settings) {
    return Boolean.TRUE.equals(getValue(settings));
  }

  public Settings.SettingsBuilder applyTo(
      final Settings.SettingsBuilder builder, final Boolean value) {
    return this.setter.apply(builder, value);
  }
}
