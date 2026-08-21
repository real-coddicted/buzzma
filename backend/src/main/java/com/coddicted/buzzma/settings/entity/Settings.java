package com.coddicted.buzzma.settings.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Settings {
  Boolean dashboardTabEnabled;
  Boolean campaignsTabEnabled;
  Boolean assignmentsTabEnabled;
  Boolean connectionsTabEnabled;
  Boolean dealTabEnabled;
  Boolean myClaimsTabEnabled;
  Boolean claimReviewEnabled;
  Boolean ticketsTabEnabled;
  Boolean feedbackTabEnabled;
  Boolean settingsTabEnabled;
  Boolean usersTabEnabled;
  Boolean myPaymentsTabEnabled;
  Boolean userPayoutsTabEnabled;
}
