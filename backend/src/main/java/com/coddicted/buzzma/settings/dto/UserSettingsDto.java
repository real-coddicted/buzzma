package com.coddicted.buzzma.settings.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserSettingsDto {
  boolean isDashboardTabEnabled;
  boolean isCampaignsTabEnabled;
  boolean isAssignmentsTabEnabled;
  boolean isConnectionsTabEnabled;
  boolean isDealTabEnabled;
  boolean isTicketsTabEnabled;
  boolean isFeedbackTabEnabled;
  boolean isSettingsTabEnabled;
}
