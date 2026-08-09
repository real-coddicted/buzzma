package com.coddicted.buzzma.campaign.persistence;

import java.math.BigInteger;
import java.util.UUID;

public interface SharedCampaignView {

  UUID getCampaignId();

  String getCode();

  String getTitle();

  String getPlatform();

  String getCampaignType();

  Integer getStartDate();

  Integer getEndDate();

  String getProductBrandName();

  String getProductName();

  String getProductLink();

  String getProductImageUrl();

  BigInteger getProductPricePaise();

  String getSellerName();

  boolean getAffiliateLinkAllowed();

  BigInteger getCampaignPricePaise();

  String getTermsAndConditions();

  UUID getCampaignOwnerId();

  String getCampaignOwnerName();
}
