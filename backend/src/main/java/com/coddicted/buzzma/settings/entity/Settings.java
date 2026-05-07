package com.coddicted.buzzma.settings.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Settings {
  boolean isDashboardTabEnabled;
  boolean isCampaignsTabEnabled;
  boolean isAssignmentsTabEnabled;
  boolean isConnectionsTabEnabled;
  boolean isDealTabEnabled;
  boolean isTicketsTabEnabled;
  boolean isFeedbackTabEnabled;
  boolean isSettingsTabEnabled;
}
