package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AssignmentResponseDto {
  UUID id;
  UUID ownerId;
  String productName;
  String productImageUrl;
  String productUrl;
  Platform platform;
  CampaignType dealType;
  BigInteger originalPricePaise;
  BigInteger offeredPricePaise;
  // Todo: commission offered
  Integer returnWindowDays;
  String termsAndConditions;
  String sellerName;
}
