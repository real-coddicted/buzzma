package com.coddicted.buzzma.campaign.dto;

import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ShareableCampaignResponseDto {

  UUID campaignId;

  String campaignTitle;

  String code;

  String platform;

  String campaignType;

  String productBrandName;

  String productImageUrl;

  Integer startDate;

  Integer endDate;

  BigInteger campaignPricePaise;

  Integer slotsAvailable;

  Integer totalSlots;
}
