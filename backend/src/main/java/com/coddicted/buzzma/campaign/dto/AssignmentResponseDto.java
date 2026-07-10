package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStatus;
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
  UUID campaignId;
  UUID ownerId;
  String productName;
  String productBrandName;
  String productImageUrl;
  String productUrl;
  Platform platform;
  CampaignType dealType;
  CampaignStatus campaignStatus;
  BigInteger originalPricePaise;
  BigInteger offeredPricePaise;
  BigInteger commissionOfferedPaise;
  Integer slotLimit;
  Integer returnWindowDays;
  String termsAndConditions;
  String sellerName;
  boolean affiliateLinkAllowed;
}
