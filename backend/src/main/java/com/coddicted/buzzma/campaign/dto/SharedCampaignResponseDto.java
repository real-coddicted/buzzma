package com.coddicted.buzzma.campaign.dto;

import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SharedCampaignResponseDto {

  UUID campaignId;

  String code;

  String title;

  String platform;

  String campaignType;

  Integer startDate;

  Integer endDate;

  String productBrandName;

  String productName;

  String productLink;

  String productImageUrl;

  BigInteger productPricePaise;

  String sellerName;

  boolean affiliateLinkAllowed;

  BigInteger campaignPricePaise;

  String termsAndConditions;

  UUID campaignOwnerId;

  String campaignOwnerName;
}
