package com.coddicted.buzzma.campaign.api;

import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Platform;
import jakarta.annotation.Nullable;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignResponseDto {

  UUID id;

  String title;

  UUID ownerId;

  Integer totalSlots;

  CampaignType campaignType;

  CampaignStatus status;

  // Product fields
  UUID productId;

  String productName;

  URL productImageUrl;

  URL productLink;

  BigInteger productPricePaise;

  Platform platform;


  // Audit fields
  Instant createdAt;

  UUID createdBy;

  Instant updatedAt;

  UUID updatedBy;

  Boolean isDeleted;

}