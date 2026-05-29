package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.URL;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignSummaryResponseDto {

  UUID campaignId;

  String title;

  URL productImageUrl;

  String productName;

  String productBrandName;

  Integer startDate;

  Integer endDate;

  CampaignStatus status;

  Platform platform;

  CampaignType type;

  Integer totalSlots;

  Integer slotsClaimed;

  BigInteger budgetPaise;
}
