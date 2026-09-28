package com.coddicted.buzzma.campaign.entity;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum CampaignStepType {
  ORDER("Order"),
  DOWNLOAD_INSTALL("Download & Install"),
  DELIVERY("Delivery"),
  RATING("Rating"),
  REVIEW("Review"),
  SELLER_FEEDBACK("Seller Feedback"),
  RETURN_WINDOW("Return Window"),
  VIEW("View"),
  LIKE("Like"),
  FOLLOW("Follow"),
  COMMENT("Comment"),
  SUBSCRIBE("Subscribe"),
  CASHBACK("Cashback");

  private final String label;

  CampaignStepType(final String label) {
    this.label = label;
  }

  /** The steps a campaign owner can pick from when configuring required screenshots. */
  public static List<CampaignStepType> selectableTypes() {
    return Arrays.stream(values()).filter(t -> t != CASHBACK).toList();
  }
}
