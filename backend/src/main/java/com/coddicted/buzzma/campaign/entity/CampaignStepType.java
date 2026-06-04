package com.coddicted.buzzma.campaign.entity;

import lombok.Getter;

@Getter
public enum CampaignStepType {
  ORDER("Order & Upload"),
  RATING("Rating"),
  REVIEW("Review"),
  RETURN_WINDOW("Return Window"),
  CASHBACK("Cashback");

  private final String label;

  CampaignStepType(final String label) {
    this.label = label;
  }
}
