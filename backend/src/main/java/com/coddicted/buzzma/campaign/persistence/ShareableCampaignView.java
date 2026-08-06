package com.coddicted.buzzma.campaign.persistence;

import java.math.BigInteger;
import java.util.UUID;

public interface ShareableCampaignView {

  UUID getCampaignId();

  String getCampaignTitle();

  String getCode();

  String getPlatform();

  String getCampaignType();

  String getProductBrandName();

  String getProductImageUrl();

  Integer getStartDate();

  Integer getEndDate();

  BigInteger getCampaignPricePaise();

  Integer getSlotsAvailable();

  Integer getTotalSlots();
}
