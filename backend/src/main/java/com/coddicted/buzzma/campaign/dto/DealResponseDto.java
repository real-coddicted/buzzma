package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigInteger;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class DealResponseDto {
  UUID id;
  UUID ownerId;
  String productName;
  String productImageUrl;
  String productUrl;
  Platform platform;
  CampaignType dealType;
  BigInteger originalPricePaise;
  BigInteger offeredPricePaise;
  Integer returnWindowDays;
  String termsAndConditions;
  String sellerName;
}
