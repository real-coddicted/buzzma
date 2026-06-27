package com.coddicted.buzzma.settings.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Settings {
  boolean dashboardTabEnabled;
  boolean campaignsTabEnabled;
  boolean assignmentsTabEnabled;
  boolean connectionsTabEnabled;
  boolean dealTabEnabled;
  boolean claimReviewEnabled;
  boolean ticketsTabEnabled;
  boolean feedbackTabEnabled;
  boolean settingsTabEnabled;
  boolean usersTabEnabled;
}
