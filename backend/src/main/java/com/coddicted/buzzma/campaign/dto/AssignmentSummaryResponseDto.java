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
public class AssignmentSummaryResponseDto {
  UUID id;
  String productName;
  String productImageUrl;
  Platform platform;
  CampaignType dealType;
  BigInteger originalPricePaise;
  BigInteger offeredPricePaise;
  Integer slotLimit;
}
