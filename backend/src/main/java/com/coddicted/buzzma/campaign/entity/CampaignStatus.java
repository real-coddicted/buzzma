package com.coddicted.buzzma.campaign.entity;

public enum CampaignStatus {
  CAMPAIGN_STATUS_DRAFT,
  // Indicates a campaign is closed manually by the owner.
  CAMPAIGN_STATUS_CLOSED,
  // When a Campaign Status is active, campaign details cannot be changed
  // Only Agency can LAUNCH a campaign.
  CAMPAIGN_STATUS_ACTIVE,
  // Campaign Assign - A brand can only assign campaigns to agency
  CAMPAIGN_STATUS_ASSIGNED,
  CAMPAIGN_STATUS_PAUSED,
  CAMPAIGN_STATUS_COMPLETED
}
